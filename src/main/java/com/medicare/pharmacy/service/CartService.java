package com.medicare.pharmacy.service;

import com.medicare.pharmacy.dto.CartDto;
import com.medicare.pharmacy.dto.CartItemDto;
import com.medicare.pharmacy.entity.Cart;
import com.medicare.pharmacy.entity.CartItem;
import com.medicare.pharmacy.entity.Inventory;
import com.medicare.pharmacy.entity.Medicine;
import com.medicare.pharmacy.entity.User;
import com.medicare.pharmacy.exception.BadRequestException;
import com.medicare.pharmacy.exception.InsufficientStockException;
import com.medicare.pharmacy.exception.ResourceNotFoundException;
import com.medicare.pharmacy.repository.CartItemRepository;
import com.medicare.pharmacy.repository.CartRepository;
import com.medicare.pharmacy.repository.InventoryRepository;
import com.medicare.pharmacy.repository.MedicineRepository;
import com.medicare.pharmacy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MedicineRepository medicineRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            return cartRepository.save(Cart.builder().user(user).build());
        });
    }

    @Transactional(readOnly = true)
    public CartDto getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return toDto(cart);
    }

    @Transactional
    public CartDto addItem(Long userId, Long medicineId, int qty) {
        if (qty <= 0) {
            throw new BadRequestException("Quantity must be positive");
        }
        Medicine med = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", medicineId));
        if (!med.isActive()) {
            throw new BadRequestException("Medicine is not available");
        }
        int available = inventoryRepository.findByMedicineId(medicineId)
                .map(Inventory::getAvailable)
                .orElse(0);
        if (available < qty) {
            throw new InsufficientStockException("Insufficient stock for " + med.getName());
        }
        Cart cart = getOrCreateCart(userId);
        CartItem existing = cartItemRepository
                .findByCartIdAndMedicineId(cart.getId(), medicineId)
                .orElse(null);
        if (existing != null) {
            int newQty = existing.getQuantity() + qty;
            if (available < newQty) {
                throw new InsufficientStockException("Insufficient stock for " + med.getName());
            }
            existing.setQuantity(newQty);
            cartItemRepository.save(existing);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .medicine(med)
                    .quantity(qty)
                    .build();
            cartItemRepository.save(item);
        }
        return toDto(cart);
    }

    @Transactional
    public CartDto updateItem(Long userId, Long itemId, int qty) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));
        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new BadRequestException("Access denied");
        }
        if (qty <= 0) {
            cartItemRepository.delete(item);
        } else {
            int available = inventoryRepository.findByMedicineId(item.getMedicine().getId())
                    .map(Inventory::getAvailable)
                    .orElse(0);
            if (available < qty) {
                throw new InsufficientStockException("Insufficient stock for " + item.getMedicine().getName());
            }
            item.setQuantity(qty);
            cartItemRepository.save(item);
        }
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));
        return toDto(cart);
    }

    @Transactional
    public CartDto removeItem(Long userId, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));
        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new BadRequestException("Access denied");
        }
        cartItemRepository.delete(item);
        Cart cart = getOrCreateCart(userId);
        return toDto(cart);
    }

    @Transactional
    public void clear(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    private CartDto toDto(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        List<CartItemDto> itemDtos = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (CartItem ci : items) {
            BigDecimal price = ci.getMedicine().getPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(subtotal);
            count += ci.getQuantity();
            itemDtos.add(CartItemDto.builder()
                    .id(ci.getId())
                    .medicineId(ci.getMedicine().getId())
                    .medicineName(ci.getMedicine().getName())
                    .price(price)
                    .quantity(ci.getQuantity())
                    .subtotal(subtotal)
                    .build());
        }
        return CartDto.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(itemDtos)
                .totalItems(count)
                .totalAmount(total)
                .build();
    }
}
