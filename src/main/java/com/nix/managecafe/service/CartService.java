package com.nix.managecafe.service;

import com.nix.managecafe.exception.ForbiddenException;
import com.nix.managecafe.exception.ResourceNotFoundException;
import com.nix.managecafe.model.Cart;
import com.nix.managecafe.model.Menu;
import com.nix.managecafe.model.User;
import com.nix.managecafe.payload.request.CartRequest;
import com.nix.managecafe.payload.response.CartResponse;
import com.nix.managecafe.repository.CartRepo;
import com.nix.managecafe.repository.MenuRepo;
import com.nix.managecafe.security.UserPrincipal;
import com.nix.managecafe.util.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final CartRepo cartRepo;
    private final MenuRepo menuRepo;

    public CartService(CartRepo cartRepo, MenuRepo menuRepo) {
        this.cartRepo = cartRepo;
        this.menuRepo = menuRepo;
    }

    public List<CartResponse> getAllCartByUser(User user) {
        List<Cart> carts = cartRepo.findAllByUser(user);
        return carts.stream().map(
                ModelMapper::mapCartToCartResponse
        ).collect(Collectors.toList());
    }

    public CartResponse updateCart(Long cartId, Long quantity, UserPrincipal currentUser) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));
        if (!Objects.equals(cart.getUser().getId(), currentUser.getId())) {
            throw new ForbiddenException("Access Denied");
        }
        cart.setQuantity(quantity);
        return ModelMapper.mapCartToCartResponse(cartRepo.save(cart));
    }

    public void deleteCart(Long cartId) {
        cartRepo.deleteById(cartId);
    }

    public CartResponse createOrUpdateCartItem(User user, CartRequest cartRequest) {
        Cart cart = cartRepo.findByUserIdAndMenuIdAndSize(user.getId(), cartRequest.getMenuId(), cartRequest.getSize())
                .orElse(new Cart());
        cart.setUser(user);
        Menu menu = menuRepo.findById(cartRequest.getMenuId()).orElseThrow(() -> new ResourceNotFoundException("Menu", "id", cartRequest.getMenuId()));
        cart.setMenu(menu);
        if (cart.getQuantity() == null) {
            cart.setQuantity(cartRequest.getQuantity());
        } else {
            cart.setQuantity(cart.getQuantity() + cartRequest.getQuantity());
        }
        cart.setSize(cartRequest.getSize());
        Cart cart1 = cartRepo.save(cart);
        return ModelMapper.mapCartToCartResponse(cart1);
    }

    @Transactional
    public void deleteAllCartFromUser(User user) {
        cartRepo.deleteByUserId(user.getId());
    }

    public int getAmountCart(User user) {
        return cartRepo.countByUserId(user.getId());
    }
}
