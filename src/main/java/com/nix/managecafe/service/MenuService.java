package com.nix.managecafe.service;

import com.nix.managecafe.exception.ResourceNotFoundException;
import com.nix.managecafe.model.*;
import com.nix.managecafe.payload.request.MenuRequest;
import com.nix.managecafe.payload.response.MenuDetailResponse;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.repository.CategoryRepo;
import com.nix.managecafe.repository.MenuDetailRepo;
import com.nix.managecafe.repository.MenuRepo;
import com.nix.managecafe.util.ModelMapper;
import com.nix.managecafe.util.ValidatePageable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = {ResourceNotFoundException.class})
public class MenuService {
    private final MenuRepo menuRepo;
    private final CategoryRepo categoryRepo;
    private final MenuDetailRepo menuDetailRepo;
    private final EntityManager entityManager;

    public MenuService(MenuRepo menuRepo, CategoryRepo categoryRepo, MenuDetailRepo menuDetailRepo, EntityManager entityManager) {
        this.menuRepo = menuRepo;
        this.categoryRepo = categoryRepo;
        this.menuDetailRepo = menuDetailRepo;
        this.entityManager = entityManager;
    }

    public PagedResponse<Menu> getAll(int page, int size, String sortBy, String sortDir) {
        ValidatePageable.invoke(page, size);
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter("deletedMenuFilter");
        filter.setParameter("isDeleted", false);

        Sort sort = (sortDir.equalsIgnoreCase("des")) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Menu> menus = menuRepo.findAll(pageable);

        session.disableFilter("deletedMenuFilter");
        return new PagedResponse<>(menus.getContent(), menus.getNumber(),
                menus.getSize(), menus.getTotalElements(), menus.getTotalPages(), menus.isLast());
    }

    public Menu getOne(Long id) {
        return menuRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", id));
    }

    public MenuDetailResponse getMenuDetail(Long id) {
        Menu menu = menuRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", id));
        return ModelMapper.mapMenuToMenuResponse(menu);
    }

    public PagedResponse<MenuDetailResponse> getAllMenuDetails(int page, int size, String sortBy, String sortDir) {
        ValidatePageable.invoke(page, size);

        Sort sort = (sortDir.equalsIgnoreCase("des")) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter("deletedMenuFilter");
        filter.setParameter("isDeleted", false);
        Page<Menu> menus = menuRepo.findAll(pageable);
        session.disableFilter("deletedMenuFilter");
        List<MenuDetailResponse> menuDetailResponses = new ArrayList<>();
        menus.getContent().forEach(
                menu -> menuDetailResponses.add(ModelMapper.mapMenuToMenuResponse(menu))
        );
        return new PagedResponse<>(menuDetailResponses, menus.getNumber(),
                menus.getSize(), menus.getTotalElements(), menus.getTotalPages(), menus.isLast());
    }

    public Menu create(MenuRequest menu) {
        Category category = new Category();
        category.setId(menu.getCategoryId());

        Menu menu1 = new Menu(menu.getName(), menu.getDescription(), category, menu.getCost(), menu.getImageUrl());
        Menu menu2 = menuRepo.save(menu1);

        menu.getMenuDetails().forEach(
                menuDetailRequest -> {
                    MenuDetail menuDetail = new MenuDetail();
                    menuDetail.setQuantity(menuDetailRequest.getQuantity());
                    menuDetail.setMenu(menu2);
                    Product product = new Product();
                    product.setId(menuDetailRequest.getProductId());
                    menuDetail.setProduct(product);
                    menuDetailRepo.save(menuDetail);
                }
        );
        return menu2;
    }

    public Menu update(Long id, MenuRequest menu) {
        Menu menu1 = menuRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", id));
        Category category = categoryRepo.findById(menu.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", menu.getCategoryId()));

        menu1.setDescription(menu.getDescription());
        menu1.setName(menu.getName());
        menu1.setCost(menu.getCost());
        menu1.setCategory(category);
        menu1.setImageUrl(menu1.getImageUrl());
        menu1.getMenuDetails().clear();

        menu.getMenuDetails().forEach(
                menuDetailRequest -> {
                    MenuDetail menuDetail;
                    menuDetail = menuDetailRepo.findById(menuDetailRequest.getId())
                            .orElse(new MenuDetail());
                    menuDetail.setQuantity(menuDetailRequest.getQuantity());
                    Product product = new Product();
                    product.setId(menuDetailRequest.getProductId());
                    menuDetail.setProduct(product);
                    menuDetail.setMenu(menu1);
                    MenuDetail menuDetail1 = menuDetailRepo.save(menuDetail);
                    menu1.getMenuDetails().add(menuDetail1);
                }
        );
        Menu menu2 = menuRepo.save(menu1);
        return menu2;
    }

    public void delete(Long id) {
        menuRepo.deleteById(id);
    }

    public List<Menu> getMenuByCategoryId(Long categoryId) {
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter("deletedMenuFilter");
        filter.setParameter("isDeleted", false);
        List<Menu> menus = menuRepo.findByCategoryId(categoryId);
        session.disableFilter("deletedMenuFilter");
        return menus;
    }

    public List<Menu> searchByName(String name) {
        String[] names = name.split(" ");
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Menu> cq = cb.createQuery(Menu.class);
        Root<Menu> root = cq.from(Menu.class);

        Predicate[] predicates = new Predicate[names.length];
        for (int i = 0; i < names.length; i++) {
            predicates[i] = cb.like(root.get("name"), "%"+ names[i] +"%");
        }
        Predicate deleted = cb.equal(root.get("deleted"), false);
        cq.select(root).where(cb.and(deleted), cb.or(predicates));
        TypedQuery<Menu> query = entityManager.createQuery(cq);
        return query.getResultList();
    }
}
