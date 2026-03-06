package com.malgn.service;

import com.malgn.entity.User;
import com.malgn.repository.UserRepository;
import com.malgn.security.CmsGrantedAuthority;
import com.malgn.security.CmsUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CmsUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public CmsUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("이름이 %s인 사용자를 찾을 수 없습니다.", username)
                ));

        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(CmsGrantedAuthority::new)
                .collect(Collectors.toList());

        return new CmsUserDetails(user.getId(), username, user.getPassword(), authorities);
    }
}
