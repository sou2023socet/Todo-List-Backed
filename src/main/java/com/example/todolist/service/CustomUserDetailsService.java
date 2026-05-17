// package com.example.todolist.service;

// import com.example.todolist.model.UserAccount;
// import com.example.todolist.repository.UserRepository;
// import com.example.todolist.security.UserPrincipal;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.stereotype.Service;

// @Service
// public class CustomUserDetailsService implements UserDetailsService {

//     private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
//     private final UserRepository userRepository;

//     public CustomUserDetailsService(UserRepository userRepository) {
//         this.userRepository = userRepository;
//     }

//     @Override
//     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//         String tenantId = null;
//         String credential = username;

//         if (username != null && username.contains("|")) {
//             int delimiterIndex = username.lastIndexOf('|');
//             credential = username.substring(0, delimiterIndex);
//             tenantId = username.substring(delimiterIndex + 1);
//         }

//         UserAccount account;
//         if (tenantId != null && !tenantId.isBlank()) {
//             if (credential.contains("@")) {
//                 account = userRepository.findByEmailAddressAndTenantId(credential, tenantId)
//                         .orElseThrow(() -> {
//                             logger.warn("User not found for tenant login attempt: {}@{}", credential, tenantId);
//                             return new UsernameNotFoundException("User not found: " + credential);
//                         });
//             } else {
//                 account = userRepository.findByUsernameAndTenantId(credential, tenantId)
//                         .orElseThrow(() -> {
//                             logger.warn("User not found for tenant login attempt: {}@{}", credential, tenantId);
//                             return new UsernameNotFoundException("User not found: " + credential);
//                         });
//             }
//         } else {
//             account = userRepository.findByUsernameOrEmailAddress(credential, credential)
//                     .orElseThrow(() -> {
//                         logger.warn("User not found for login attempt: {}", credential);
//                         return new UsernameNotFoundException("User not found: " + credential);
//                     });
//         }

//         logger.debug("Loaded user for authentication: {} (tenant={})", account.getUsername(), account.getTenantId());
//         return UserPrincipal.from(account);
//     }
// }

package com.example.todolist.service;

import com.example.todolist.model.UserAccount;
import com.example.todolist.repository.UserRepository;
import com.example.todolist.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        String tenantId = null;
        String credential = username;

        if (username != null && username.contains("|")) {

            int delimiterIndex = username.lastIndexOf('|');

            credential = username.substring(0, delimiterIndex);

            tenantId = username.substring(delimiterIndex + 1);
        }

        // FINAL VARIABLES FOR LAMBDA USAGE
        final String finalCredential = credential;
        final String finalTenantId = tenantId;

        UserAccount account;

        if (finalTenantId != null && !finalTenantId.isBlank()) {

            if (finalCredential.contains("@")) {

                account = userRepository
                        .findByEmailAddressAndTenantId(
                                finalCredential,
                                finalTenantId)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "User not found for tenant login attempt: {}@{}",
                                    finalCredential,
                                    finalTenantId);

                            return new UsernameNotFoundException(
                                    "User not found: " + finalCredential);
                        });

            } else {

                account = userRepository
                        .findByUsernameAndTenantId(
                                finalCredential,
                                finalTenantId)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "User not found for tenant login attempt: {}@{}",
                                    finalCredential,
                                    finalTenantId);

                            return new UsernameNotFoundException(
                                    "User not found: " + finalCredential);
                        });
            }

        } else {

            account = userRepository
                    .findByUsernameOrEmailAddress(
                            finalCredential,
                            finalCredential)
                    .orElseThrow(() -> {

                        logger.warn(
                                "User not found for login attempt: {}",
                                finalCredential);

                        return new UsernameNotFoundException(
                                "User not found: " + finalCredential);
                    });
        }

        logger.debug(
                "Loaded user for authentication: {} (tenant={})",
                account.getUsername(),
                account.getTenantId());

        return UserPrincipal.from(account);
    }
}
