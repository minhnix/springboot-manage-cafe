package com.nix.managecafe.controller;

import com.nix.managecafe.exception.AuthenticationException;
import com.nix.managecafe.payload.request.CartRequest;
import com.nix.managecafe.payload.response.CartResponse;
import com.nix.managecafe.security.CurrentUser;
import com.nix.managecafe.security.UserPrincipal;
import com.nix.managecafe.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/carts")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public List<CartResponse> getCartByCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        if (userPrincipal == null) throw new AuthenticationException("Full authentication to get resource");
        return cartService.getAllCartByUser(userPrincipal.getUser());
    }

    @GetMapping("/amount")
    public int getAmountCart(@CurrentUser UserPrincipal userPrincipal) {
        if (userPrincipal == null) throw new AuthenticationException("Full authentication to get resource");
        return cartService.getAmountCart(userPrincipal.getUser());
    }

    @PostMapping
    public CartResponse createOrUpdateCartItem(@CurrentUser UserPrincipal userPrincipal, @RequestBody CartRequest cartRequest) {
        if (userPrincipal == null) throw new AuthenticationException("Full authentication to get resource");
        return cartService.createOrUpdateCartItem(userPrincipal.getUser(), cartRequest);
    }

    @PutMapping("/{id}")
    public CartResponse updateCart(@PathVariable("id") Long id, @RequestBody CartRequest cartRequest, @CurrentUser UserPrincipal currentUser) {
        if (currentUser == null) throw new AuthenticationException("Full authentication to get resource");
        return cartService.updateCart(id, cartRequest.getQuantity(), currentUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCart(@PathVariable("id") Long id) {
        cartService.deleteCart(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllCart(@CurrentUser UserPrincipal userPrincipal) {
        cartService.deleteAllCartFromUser(userPrincipal.getUser());
    }
}
