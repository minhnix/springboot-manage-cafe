package com.nix.managecafe.service;

import com.nix.managecafe.exception.AppException;
import com.nix.managecafe.exception.BadRequestException;
import com.nix.managecafe.exception.InvalidPasswordException;
import com.nix.managecafe.exception.ResourceNotFoundException;
import com.nix.managecafe.model.Address;
import com.nix.managecafe.model.Role;
import com.nix.managecafe.model.enumname.RoleName;
import com.nix.managecafe.model.User;
import com.nix.managecafe.payload.request.SignUpRequest;
import com.nix.managecafe.payload.request.UpdatePasswordRequest;
import com.nix.managecafe.payload.request.UpdateUserRequest;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.repository.OrderRepo;
import com.nix.managecafe.repository.RoleRepo;
import com.nix.managecafe.repository.TimeSheetRepo;
import com.nix.managecafe.repository.UserRepo;
import com.nix.managecafe.security.UserPrincipal;
import com.nix.managecafe.util.ValidatePageable;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.Collections;

@Service
public class UserService {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final TimeSheetRepo timeSheetRepo;
    private final OrderRepo orderRepo;
    private final EmailSender emailSender;
    Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepo userRepo, RoleRepo roleRepo, PasswordEncoder passwordEncoder, TimeSheetRepo timeSheetRepo, OrderRepo orderRepo, EmailSender emailSender) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.timeSheetRepo = timeSheetRepo;
        this.orderRepo = orderRepo;
    }

    public User createUser(SignUpRequest signUpRequest, RoleName roleName) {

        if (userRepo.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email đã sử dụng!!!");
        }

        if (userRepo.existsByUsername(signUpRequest.getUsername())) {
            throw new BadRequestException("Username already in use!!!");
        }

        if (userRepo.existsByPhoneNumber(signUpRequest.getPhoneNumber())) {
            throw new BadRequestException("Phone Number already in use!!!");
        }

        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(), signUpRequest.getPassword(), signUpRequest.getPhoneNumber());

        Role userRole = roleRepo.findByName(roleName)
                .orElseThrow(() -> new AppException("User Role not set."));

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(userRole));
        if (signUpRequest.getFirstname() != null && signUpRequest.getLastname() != null) {
            user.setFirstname(signUpRequest.getFirstname());
            user.setLastname(signUpRequest.getLastname());
        }

        return userRepo.save(user);
    }

    public long getAmountOfUserByRoleId(Long roleId) {
        return userRepo.countByRolesId(roleId);
    }


    @Transactional(rollbackFor = {ResourceNotFoundException.class})
    @CacheEvict(value = "usersById")
    public void deleteByUserId(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        timeSheetRepo.deleteByUser(user);
        orderRepo.updateOrderByStaffIdWhenDelete(id);
        userRepo.delete(user);
    }

    @CachePut(value = "usersById")
    public User updateUser(Long id, UpdateUserRequest updateRequest) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Id", "userId", id));
        if (updateRequest.getFirstname() != null) {
            user.setFirstname(updateRequest.getFirstname());
        }
        if (updateRequest.getLastname() != null) {
            user.setLastname(updateRequest.getLastname());
        }
        if (updateRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        if (updateRequest.getEmail() != null) {
            user.setEmail(updateRequest.getEmail());
        }
        Address address = user.getAddress();
        if (address == null) address = new Address();
        if (updateRequest.getAddress() != null) {
            Address address1 = updateRequest.getAddress();
            if (address1.getCity() != null) {
                address.setCity(address1.getCity());
            }
            if (address1.getWard() != null) {
                address.setWard(address1.getWard());
            }
            if (address1.getRoad() != null) {
                address.setRoad(address1.getRoad());
            }
            if (address1.getDistrict() != null) {
                address.setDistrict(address1.getDistrict());
            }
            if (address1.getDistrictCode() != null) {
                address.setDistrictCode(address1.getDistrictCode());
            }
            if (address1.getWardCode() != null) {
                address.setWardCode(address1.getWardCode());
            }
            user.setAddress(address);
        }
        return userRepo.save(user);
    }

    public PagedResponse<User> getAll(int page, int size, String sortBy, String sortDir, Long roleId, String keyword) {
        ValidatePageable.invoke(page, size);

        Sort sort = (sortDir.equalsIgnoreCase("des")) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users;
        if (roleId == null)
            users = userRepo.findAll(pageable);
        else
            users = userRepo.findUserByRolesId(roleId, pageable);
        return new PagedResponse<>(users.getContent(), users.getNumber(),
                users.getSize(), users.getTotalElements(), users.getTotalPages(), users.isLast());
    }

    public User getOneUser(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    public User getProfile(UserPrincipal userPrincipal) {
        return userPrincipal.getUser();
    }

    public void updatePassword(UserPrincipal userPrincipal, UpdatePasswordRequest updatePasswordRequest) throws InvalidPasswordException {
        if (passwordEncoder.matches(updatePasswordRequest.getOldPassword(), userPrincipal.getPassword())) {
            User user = userPrincipal.getUser();
            user.setPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
            userRepo.save(user);
        } else if (updatePasswordRequest.getOldPassword().equals(updatePasswordRequest.getNewPassword())) {
            throw new BadRequestException("Mật khẩu mới trùng mật khẩu mới");
        } else {
            throw new InvalidPasswordException(false, "Mật khẩu cũ không đúng");
        }
    }
    @Transactional(rollbackFor = {MessagingException.class, UnsupportedEncodingException.class})
    public void sendResetPasswordEmail(String email) throws MessagingException, UnsupportedEncodingException {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        String toAddress = user.getEmail();
        String senderName = "DUT Milk Tea";
        String subject = "Reset password";
        String content = "Dear [[name]],<br>"
                + "New password: [[password]]<br>"
                + "DUT Milk Tea.";
        String password = RandomStringUtils.randomAlphanumeric(10);
        content = content.replace("[[name]]", user.getUsername());
        content = content.replace("[[password]]", password);

        emailSender.sendTo(toAddress, senderName, subject, content);
        user.setPassword(passwordEncoder.encode(password));
        userRepo.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    }
}
