# mailsmr.io Backend



## Tasks

### Authentication Module

- [x] authenticate
- [x] reauthenticate with refresh token
- [x] list all valid refresh tokens
- [ ] refresh token invalidation
- [x] REST
- [ ] 2FA

### User Management Module

- [x] Create User
- [x] Delete User
- [x] Patch User
- [x] Get User Information
- [x] REST

### Mail Management Module

- [ ] Load Emails from Folder
- [x] Load Email
- [x] Load Folders
- [x] Subscribe for new messages on folder
- [ ] Copy Email to other folder
- [ ] Move Email to other folder
- [ ] Test Connection
- [ ] REST

### Mail Send Module

- [ ] Send Mails
- [ ] Test Connection
- [ ] Delayed Sending (Special Permission required)

### Mail Account Management Module

- [ ] Create Mail Account
- [ ] Delete Mail Account
- [ ] Update Mail Account
- [ ] Auto Fetch Connection Details
- [ ] Add Signature

### General

#### Web Security

- [ ] CORS (Cross Origin Request Security)
- [ ] CSP (Content Security Policy)
- [ ] Input Validation on each REST endpoint



## Todos

- [ ] Add tests for subscriptions for email server that does not support `idle`
- [ ] Remove all references to MEWEC