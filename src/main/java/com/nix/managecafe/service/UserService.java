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
import com.nix.managecafe.repository.RoleRepo;
import com.nix.managecafe.repository.UserRepo;
import com.nix.managecafe.security.UserPrincipal;
import com.nix.managecafe.util.ValidatePageable;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang3.RandomStringUtils;
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
    private final JavaMailSender mailSender;

    Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepo userRepo, RoleRepo roleRepo, PasswordEncoder passwordEncoder, JavaMailSender mailSender) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    public User createUser(SignUpRequest signUpRequest, RoleName roleName) {
        if (userRepo.existsByUsername(signUpRequest.getUsername())) {
            throw new BadRequestException("Username already in use!!!");
        }

        if (userRepo.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email Address already in use!!!");
        }

        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(), signUpRequest.getPassword(), signUpRequest.getPhoneNumber());

        Role userRole = roleRepo.findByName(roleName)
                .orElseThrow(() -> new AppException("User Role not set."));

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(userRole));

        User user1 = userRepo.save(user);
        return user1;
    }


    public void removeByUsername(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

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

    public PagedResponse<User> getAll(int page, int size, String sortBy, String sortDir) {
        ValidatePageable.invoke(page, size);

        Sort sort = (sortDir.equalsIgnoreCase("des")) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = userRepo.findAll(pageable);

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
        } else {
            throw new InvalidPasswordException(false, "Invalid old password!!");
        }
    }
    @Transactional(rollbackFor = {MessagingException.class, UnsupportedEncodingException.class})
    public void sendResetPasswordEmail(User user) throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = "bhminh322@gmail.com";
        String senderName = "NIX";
        String subject = "Reset password";
        String content = "Dear [[name]],<br>"
                + "New password: [[password]]<br>"
                + "NIX.";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        String password = RandomStringUtils.randomAlphanumeric(10);
        content = content.replace("[[name]]", user.getUsername());
        content = content.replace("[[password]]", password);

        user.setPassword(passwordEncoder.encode(password));
        userRepo.save(user);
        helper.setText(content, true);

        mailSender.send(message);
    }
}
