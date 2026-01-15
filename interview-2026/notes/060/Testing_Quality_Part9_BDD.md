# BDD (Behavior-Driven Development): Given-When-Then, Cucumber

## Overview

Behavior-Driven Development (BDD) is a software development methodology that extends TDD by focusing on the behavior of an application from the perspective of its stakeholders. BDD uses natural language to describe behavior in a format that both technical and non-technical team members can understand.

## BDD Core Concepts

### 1. Given-When-Then Structure

BDD uses a structured format to describe scenarios:

```
GIVEN [initial context]
WHEN  [event occurs]
THEN  [expected outcome]
```

**Example:**
```
GIVEN a user has logged into the system
WHEN they click the "Add to Cart" button
THEN the item should be added to their cart
AND the cart count should increase by 1
```

## BDD vs TDD Comparison

```
┌─────────────────────────────────────────────────────────┐
│                    TDD Approach                        │
├─────────────────────────────────────────────────────────┤
│ Focus: Technical implementation                        │
│ Language: Code (Java, Python, etc.)                    │
│ Audience: Developers                                    │
│ Example: testCalculateDiscount_shouldReturn10Percent() │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                    BDD Approach                         │
├─────────────────────────────────────────────────────────┤
│ Focus: Business behavior                                │
│ Language: Natural language (English)                    │
│ Audience: Business, QA, Developers                    │
│ Example: Given a regular customer, when they purchase,  │
│          then they should receive a 10% discount        │
└─────────────────────────────────────────────────────────┘
```

## BDD Structure: Feature Files

### Feature File Format

```gherkin
Feature: User Authentication
  As a user
  I want to log into the system
  So that I can access my account

  Scenario: Successful login with valid credentials
    Given the user is on the login page
    When they enter valid username "john@example.com"
    And they enter valid password "password123"
    And they click the login button
    Then they should be redirected to the dashboard
    And they should see a welcome message

  Scenario: Failed login with invalid credentials
    Given the user is on the login page
    When they enter invalid username "wrong@example.com"
    And they enter password "wrongpassword"
    And they click the login button
    Then they should see an error message "Invalid credentials"
    And they should remain on the login page
```

## BDD Keywords Explained

### Feature
Describes the feature being tested
```gherkin
Feature: Shopping Cart
  As a customer
  I want to add items to my cart
  So that I can purchase multiple items together
```

### Scenario
A single test case describing a specific behavior
```gherkin
Scenario: Add item to empty cart
  Given I have an empty shopping cart
  When I add a "Book" for $10.00
  Then my cart should contain 1 item
  And the total should be $10.00
```

### Given (Preconditions)
Sets up the initial state
```gherkin
Given the user is logged in
Given the product "Laptop" exists in the catalog
Given the shopping cart is empty
```

### When (Actions)
Describes the action or event
```gherkin
When the user clicks "Add to Cart"
When the user enters their credit card number
When the system processes the payment
```

### Then (Outcomes)
Describes the expected outcome
```gherkin
Then the item should be added to the cart
Then the payment should be processed successfully
Then an email confirmation should be sent
```

### And / But (Continuation)
Extends previous steps
```gherkin
Given the user is logged in
And they have items in their cart
But the cart total is less than $50
```

### Background
Common steps for all scenarios in a feature
```gherkin
Feature: User Profile Management

Background:
  Given the user is logged in
  And they are on the profile page

Scenario: Update email address
  When they change their email to "new@example.com"
  Then the email should be updated

Scenario: Update phone number
  When they change their phone to "123-456-7890"
  Then the phone should be updated
```

### Scenario Outline (Data-Driven)
Tests multiple scenarios with different data
```gherkin
Scenario Outline: Calculate discount based on customer type
  Given a <customer_type> customer
  When they purchase items worth $<amount>
  Then they should receive a <discount>% discount
  And the final amount should be $<final_amount>

  Examples:
    | customer_type | amount | discount | final_amount |
    | Regular       | 100    | 10       | 90           |
    | Premium       | 100    | 20       | 80           |
    | VIP           | 100    | 30       | 70           |
```

## Cucumber Framework

Cucumber is a tool that supports BDD by allowing you to write specifications in plain English and execute them as automated tests.

### Cucumber Architecture

```
┌─────────────────────────────────────────────────────┐
│              Feature Files (.feature)               │
│         (Gherkin - Natural Language)                │
└────────────────────┬────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│            Step Definitions (.java/.js)              │
│         (Code that implements steps)                 │
└────────────────────┬────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│              Application Code                        │
│         (Actual implementation)                     │
└─────────────────────────────────────────────────────┘
```

### Step Definitions

Step definitions connect Gherkin steps to code:

```java
@Given("the user is on the login page")
public void userIsOnLoginPage() {
    driver.get("https://example.com/login");
}

@When("they enter valid username {string}")
public void enterUsername(String username) {
    driver.findElement(By.id("username")).sendKeys(username);
}

@When("they enter valid password {string}")
public void enterPassword(String password) {
    driver.findElement(By.id("password")).sendKeys(password);
}

@When("they click the login button")
public void clickLoginButton() {
    driver.findElement(By.id("login-btn")).click();
}

@Then("they should be redirected to the dashboard")
public void verifyDashboardRedirect() {
    String currentUrl = driver.getCurrentUrl();
    assertTrue(currentUrl.contains("/dashboard"));
}

@Then("they should see a welcome message")
public void verifyWelcomeMessage() {
    WebElement message = driver.findElement(By.id("welcome"));
    assertTrue(message.isDisplayed());
}
```

## BDD Workflow

```
┌─────────────────────────────────────────────────────────┐
│                    BDD Workflow                        │
└─────────────────────────────────────────────────────────┘

1. Business Analyst / Product Owner
   │
   ▼
   Write Feature in Gherkin
   (Natural Language)
   │
   ▼
2. Team Review
   │
   ▼
   Refine Feature File
   │
   ▼
3. Developer
   │
   ▼
   Write Step Definitions
   (Implementation)
   │
   ▼
4. Run Cucumber
   │
   ▼
   Tests Execute
   │
   ▼
5. Review Results
   │
   ▼
   Pass? → Done
   Fail? → Fix & Repeat
```

## BDD Example: E-Commerce Checkout

### Feature File
```gherkin
Feature: Checkout Process
  As a customer
  I want to complete my purchase
  So that I can receive my items

  Scenario: Successful checkout with credit card
    Given I have items in my shopping cart
    And I am on the checkout page
    When I enter my credit card details
    And I click "Complete Purchase"
    Then my order should be confirmed
    And I should receive a confirmation email
    And the items should be removed from my cart

  Scenario: Checkout fails with invalid card
    Given I have items in my shopping cart
    And I am on the checkout page
    When I enter an invalid credit card number
    And I click "Complete Purchase"
    Then I should see an error message
    And my order should not be processed
    And the items should remain in my cart
```

### Step Definitions (Java)
```java
public class CheckoutSteps {
    private ShoppingCart cart;
    private CheckoutPage checkoutPage;
    private OrderService orderService;
    
    @Given("I have items in my shopping cart")
    public void iHaveItemsInCart() {
        cart = new ShoppingCart();
        cart.addItem(new Item("Book", 10.0));
        cart.addItem(new Item("Pen", 2.0));
    }
    
    @Given("I am on the checkout page")
    public void iAmOnCheckoutPage() {
        checkoutPage = new CheckoutPage(driver);
        checkoutPage.navigate();
    }
    
    @When("I enter my credit card details")
    public void enterCreditCardDetails() {
        checkoutPage.enterCardNumber("4111111111111111");
        checkoutPage.enterExpiryDate("12/25");
        checkoutPage.enterCVV("123");
    }
    
    @When("I enter an invalid credit card number")
    public void enterInvalidCard() {
        checkoutPage.enterCardNumber("0000000000000000");
        checkoutPage.enterExpiryDate("12/25");
        checkoutPage.enterCVV("123");
    }
    
    @When("I click {string}")
    public void clickButton(String buttonText) {
        checkoutPage.clickButton(buttonText);
    }
    
    @Then("my order should be confirmed")
    public void verifyOrderConfirmed() {
        Order order = orderService.getLatestOrder();
        assertNotNull(order);
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }
    
    @Then("I should receive a confirmation email")
    public void verifyConfirmationEmail() {
        Email email = emailService.getLatestEmail();
        assertNotNull(email);
        assertEquals("Order Confirmation", email.getSubject());
    }
    
    @Then("I should see an error message")
    public void verifyErrorMessage() {
        assertTrue(checkoutPage.isErrorMessageDisplayed());
    }
}
```

## BDD Best Practices

### 1. Write Scenarios from User Perspective
```gherkin
# BAD
Given the database has a user record
When the API endpoint /users/{id} is called
Then HTTP 200 should be returned

# GOOD
Given a user exists in the system
When I view my profile
Then I should see my account information
```

### 2. Keep Scenarios Independent
- Each scenario should be able to run alone
- Don't depend on execution order
- Use Background for common setup

### 3. Use Descriptive Names
```gherkin
# BAD
Scenario: Test 1

# GOOD
Scenario: User cannot login with incorrect password
```

### 4. Keep Steps Reusable
```java
// BAD: Duplicate code
@Given("user is logged in as admin")
public void loginAsAdmin() { ... }

@Given("user is logged in as customer")
public void loginAsCustomer() { ... }

// GOOD: Parameterized
@Given("user is logged in as {string}")
public void loginAs(String role) { ... }
```

### 5. Use Data Tables for Complex Data
```gherkin
Given the following products exist:
  | Name  | Price | Stock |
  | Book  | 10.00 | 100   |
  | Pen   | 2.00  | 50    |
  | Laptop| 500.00| 10    |
```

## BDD Tools

### Java
- **Cucumber-JVM**: BDD framework
- **JBehave**: Alternative BDD framework
- **Serenity**: BDD with reporting

### JavaScript
- **Cucumber.js**: JavaScript BDD
- **Jest-Cucumber**: Jest integration

### Python
- **Behave**: BDD framework
- **Lettuce**: BDD framework

### .NET
- **SpecFlow**: BDD for .NET
- **xBehave.net**: xUnit integration

## BDD Benefits

### 1. Shared Understanding
- Business and technical teams speak same language
- Clear requirements
- Reduced misunderstandings

### 2. Living Documentation
- Executable specifications
- Always up-to-date
- Examples of system behavior

### 3. Better Test Coverage
- Tests written from user perspective
- Covers real scenarios
- Business-valuable tests

### 4. Collaboration
- Business analysts write scenarios
- Developers implement
- QA validates

## BDD Challenges

### Challenge 1: Over-specification
**Solution**: Focus on behavior, not implementation details

### Challenge 2: Maintenance
**Solution**: Keep steps reusable, use page objects

### Challenge 3: Slow Execution
**Solution**: Use unit tests for detailed logic, BDD for workflows

### Challenge 4: Learning Curve
**Solution**: Start simple, gradually add complexity

## BDD vs TDD: When to Use

| Aspect | TDD | BDD |
|--------|-----|-----|
| Focus | Technical | Business |
| Audience | Developers | Business + Developers |
| Language | Code | Natural Language |
| Use Case | Unit/Component Tests | Integration/E2E Tests |
| Granularity | Fine-grained | Coarse-grained |

**Best Practice**: Use both!
- TDD for unit tests (technical)
- BDD for integration/E2E tests (business)

## Summary

BDD is a methodology that:
- Uses natural language (Gherkin) to describe behavior
- Follows Given-When-Then structure
- Bridges business and technical teams
- Creates living documentation
- Uses tools like Cucumber for automation

**Key Principles:**
- Write from user perspective
- Use plain English
- Focus on behavior
- Create executable specifications
- Collaborate across teams
