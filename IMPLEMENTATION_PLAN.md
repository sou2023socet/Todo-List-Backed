# Implementation Plan: Todo List Backend and Dashboard

## Goal
Ensure the backend and web dashboard are aligned for local development, tenant-aware authentication, and current API usage.

## Scope
- Fix backend tenant-based login behavior
- Verify JWT auth flow for `todo` APIs
- Update frontend backend URL and request flow
- Document implementation and current usage in README

## Tasks

### Backend
1. Add tenant-specific repository methods in `UserRepository`
   - `findByUsernameAndTenantId`
   - `findByEmailAddressAndTenantId`
2. Extend `AuthService` to support tenant-specific user lookup
   - `findByUsernameOrEmailAndTenantId`
3. Update `CustomUserDetailsService` to parse login credentials with tenant context
   - support `username|tenantId` encoded principal
4. Change `AuthController.login` to authenticate using tenant-specific principal
   - include tenantId in login request token
   - use tenant-aware lookup when issuing JWT
5. Revalidate local dev HTTPS/HTTP configuration
   - confirm backend docs match implementation

### Frontend
1. Confirm `ApiService` points to local backend `http://localhost:8080/api`
2. Verify login/register request shapes include `tenantId`
3. Confirm `AuthInterceptor` sends JWT bearer token for protected requests

### Docs
1. Update `README.md` to show correct local access, auth API usage, and JWT header usage
2. Update `TECHNICAL_SPECIFICATION.md` with tenant-aware login and local dev access

## Verification
- Run Maven compile for backend
- Optionally run frontend build or evaluate route/auth flow correctness

## Notes
- Backend login should require `tenantId` for tenant isolation
- Token-based auth should be the primary access method for web dashboard APIs
- Local development bypasses HTTPS by default and uses `http://localhost:8080`
