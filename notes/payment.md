## Payment Service 

### Sync 
![Sync Payment](./images/Payment%20Service%20Sync.png)
#### Scenario:
- We're a provider for cross-platform payments.
- Supported payment providers are for example: Paypal, Wise, Stripe, bank accounts, ... .
- Users can pay from whatever account they want into whatever account they want.
- Example: a user can transfer their own money from a PayPal account into a Wise account or a bank account.
- Example: a user can pay a seller for a service or product with money from any supported account, as long as the seller uses a supported account.
- the provider services are "wrappers / facades" for the external providers
- the external providers APIs are synchronous

#### Why this scenario?
- Because it's very brittle to implement this synchronously.
- It would require a 2-phase commit to move money from one account to another.
- Since we don't have the accounts in our direct control (they're controlled by the external payment providers), a 2-phase commit is impossible.
- Instead, we need to embrace event-driven architecture.
- Since a lot can go wrong, we want to record every step of the way (candidate for event-sourcing).
- We want to make it idempotent, so we can restart a failed transfer knowing that it won't break anything.

### ASync
![Async Payment](./images/Payment%20Service%20Async.png)
