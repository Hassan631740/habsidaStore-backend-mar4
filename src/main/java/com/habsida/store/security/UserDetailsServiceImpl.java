package com.habsida.store.security;

import com.habsida.store.entity.User;
import com.habsida.store.entity.UserRole;
import com.habsida.store.repository.UserRepository;
import com.habsida.store.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        List<String> roleNames = userRoleRepository.findByUserId(user.getId()).stream()
                .map(ur -> ur.getRole() != null ? ur.getRole().getName() : null)
                .filter(name -> name != null)
                .collect(Collectors.toList());
        return new AuthUser(user, roleNames);
    }
}
