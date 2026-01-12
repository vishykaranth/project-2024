# String Problems in Python - Part 7: String Validation & Formatting

## Overview

This part covers string validation, formatting, and transformation problems.

---

## Common Patterns

### Pattern 1: Character Validation
**Use Case**: Check character properties
**Time Complexity**: O(n)
**Space Complexity**: O(1)

### Pattern 2: Formatting Rules
**Use Case**: Apply formatting rules
**Time Complexity**: O(n)
**Space Complexity**: O(n)

### Pattern 3: State Validation
**Use Case**: Validate state transitions
**Time Complexity**: O(n)
**Space Complexity**: O(1)

---

## Problem 1: Valid Email Address

### Problem Statement
Validate if string is a valid email address.

### Solution
```python
import re

def is_valid_email(email):
    """
    Validate email using regex.
    Time: O(n), Space: O(1)
    """
    pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
    return bool(re.match(pattern, email))

# Manual validation
def is_valid_email_manual(email):
    """
    Validate email manually.
    Time: O(n), Space: O(1)
    """
    if not email or '@' not in email:
        return False
    
    parts = email.split('@')
    if len(parts) != 2:
        return False
    
    local, domain = parts
    
    # Validate local part
    if not local or len(local) > 64:
        return False
    if local.startswith('.') or local.endswith('.'):
        return False
    if '..' in local:
        return False
    
    # Validate domain part
    if not domain or '.' not in domain:
        return False
    if domain.startswith('.') or domain.endswith('.'):
        return False
    
    return True

# Test
print(is_valid_email("user@example.com"))  # True
print(is_valid_email("invalid.email"))  # False
print(is_valid_email("user@domain"))  # False
```

### Pattern Used: Regex / Character Validation

---

## Problem 2: Valid Phone Number

### Problem Statement
Validate phone number in various formats.

### Solution
```python
import re

def is_valid_phone(phone):
    """
    Validate phone number.
    Time: O(n), Space: O(1)
    """
    # Remove common separators
    cleaned = re.sub(r'[-\s()]', '', phone)
    
    # Check if all digits
    if not cleaned.isdigit():
        return False
    
    # Check length (10 or 11 digits)
    if len(cleaned) == 10:
        return True
    elif len(cleaned) == 11 and cleaned[0] == '1':
        return True
    
    return False

# Format phone number
def format_phone(phone):
    """
    Format phone number to (XXX) XXX-XXXX.
    Time: O(n), Space: O(n)
    """
    cleaned = re.sub(r'[^\d]', '', phone)
    
    if len(cleaned) == 10:
        return f"({cleaned[:3]}) {cleaned[3:6]}-{cleaned[6:]}"
    elif len(cleaned) == 11 and cleaned[0] == '1':
        cleaned = cleaned[1:]
        return f"({cleaned[:3]}) {cleaned[3:6]}-{cleaned[6:]}"
    
    return phone

# Test
print(is_valid_phone("123-456-7890"))  # True
print(format_phone("1234567890"))  # "(123) 456-7890"
```

### Pattern Used: Character Validation

---

## Problem 3: Valid Credit Card Number (Luhn Algorithm)

### Problem Statement
Validate credit card number using Luhn algorithm.

### Solution
```python
def is_valid_credit_card(card_number):
    """
    Validate credit card using Luhn algorithm.
    Time: O(n), Space: O(1)
    """
    # Remove spaces and dashes
    card_number = card_number.replace(' ', '').replace('-', '')
    
    if not card_number.isdigit():
        return False
    
    # Luhn algorithm
    total = 0
    reverse_digits = card_number[::-1]
    
    for i, digit in enumerate(reverse_digits):
        num = int(digit)
        if i % 2 == 1:  # Every second digit from right
            num *= 2
            if num > 9:
                num -= 9
        total += num
    
    return total % 10 == 0

# Test
print(is_valid_credit_card("4532 1488 0343 6467"))  # True (example)
print(is_valid_credit_card("1234 5678 9012 3456"))  # False
```

### Pattern Used: Algorithm Validation

---

## Problem 4: Format Currency

### Problem Statement
Format number as currency string.

### Solution
```python
def format_currency(amount, currency="USD"):
    """
    Format number as currency.
    Time: O(n), Space: O(n)
    """
    # Handle negative
    is_negative = amount < 0
    amount = abs(amount)
    
    # Split integer and decimal parts
    integer_part = int(amount)
    decimal_part = int((amount - integer_part) * 100)
    
    # Format integer part with commas
    integer_str = str(integer_part)
    formatted_integer = []
    
    for i, digit in enumerate(reversed(integer_str)):
        if i > 0 and i % 3 == 0:
            formatted_integer.append(',')
        formatted_integer.append(digit)
    
    integer_str = ''.join(reversed(formatted_integer))
    
    # Format decimal part
    decimal_str = f"{decimal_part:02d}"
    
    # Combine
    result = f"{currency} {integer_str}.{decimal_str}"
    if is_negative:
        result = "-" + result
    
    return result

# Using locale
def format_currency_locale(amount, currency="USD"):
    import locale
    locale.setlocale(locale.LC_ALL, 'en_US.UTF-8')
    return locale.currency(amount, grouping=True)

# Test
print(format_currency(1234567.89))  # "USD 1,234,567.89"
print(format_currency(-1234.5))  # "-USD 1,234.50"
```

### Pattern Used: Number Formatting

---

## Problem 5: CamelCase to snake_case

### Problem Statement
Convert CamelCase string to snake_case.

### Solution
```python
def camel_to_snake(s):
    """
    Convert CamelCase to snake_case.
    Time: O(n), Space: O(n)
    """
    result = []
    
    for i, char in enumerate(s):
        if char.isupper():
            if i > 0:
                result.append('_')
            result.append(char.lower())
        else:
            result.append(char)
    
    return ''.join(result)

# Test
print(camel_to_snake("CamelCase"))  # "camel_case"
print(camel_to_snake("getUserName"))  # "get_user_name"
```

### Pattern Used: Character Transformation

---

## Problem 6: snake_case to CamelCase

### Problem Statement
Convert snake_case string to CamelCase.

### Solution
```python
def snake_to_camel(s, capitalize_first=False):
    """
    Convert snake_case to CamelCase.
    Time: O(n), Space: O(n)
    """
    words = s.split('_')
    
    if capitalize_first:
        result = [word.capitalize() for word in words]
    else:
        result = [words[0]] + [word.capitalize() for word in words[1:]]
    
    return ''.join(result)

# Test
print(snake_to_camel("snake_case_string"))  # "snakeCaseString"
print(snake_to_camel("snake_case_string", True))  # "SnakeCaseString"
```

### Pattern Used: Word Transformation

---

## Problem 7: Valid IPv4 Address

### Problem Statement
Validate if string is a valid IPv4 address.

### Solution
```python
def is_valid_ipv4(ip):
    """
    Validate IPv4 address.
    Time: O(n), Space: O(1)
    """
    parts = ip.split('.')
    
    if len(parts) != 4:
        return False
    
    for part in parts:
        if not part.isdigit():
            return False
        
        num = int(part)
        if num < 0 or num > 255:
            return False
        
        # Check for leading zeros
        if len(part) > 1 and part[0] == '0':
            return False
    
    return True

# Test
print(is_valid_ipv4("192.168.1.1"))  # True
print(is_valid_ipv4("256.1.1.1"))  # False
print(is_valid_ipv4("01.1.1.1"))  # False
```

### Pattern Used: Validation with Rules

---

## Problem 8: Valid IPv6 Address

### Problem Statement
Validate if string is a valid IPv6 address.

### Solution
```python
def is_valid_ipv6(ip):
    """
    Validate IPv6 address.
    Time: O(n), Space: O(1)
    """
    # Handle compressed form (::)
    if '::' in ip:
        parts = ip.split('::')
        if len(parts) > 2:
            return False
        left = parts[0].split(':') if parts[0] else []
        right = parts[1].split(':') if len(parts) > 1 and parts[1] else []
        total_parts = len(left) + len(right)
        if total_parts > 7:
            return False
    else:
        parts = ip.split(':')
        if len(parts) != 8:
            return False
    
    # Validate each part
    hex_chars = set('0123456789abcdefABCDEF')
    for part in ip.split(':'):
        if not part:
            continue
        if len(part) > 4:
            return False
        if not all(c in hex_chars for c in part):
            return False
    
    return True

# Test
print(is_valid_ipv6("2001:0db8:85a3:0000:0000:8a2e:0370:7334"))  # True
print(is_valid_ipv6("2001:db8:85a3::8a2e:370:7334"))  # True
```

### Pattern Used: Complex Validation

---

## Problem 9: Format String with Placeholders

### Problem Statement
Format string with named placeholders like "Hello {name}, you have {count} messages".

### Solution
```python
def format_string(template, **kwargs):
    """
    Format string with named placeholders.
    Time: O(n), Space: O(n)
    """
    result = []
    i = 0
    
    while i < len(template):
        if template[i] == '{':
            # Find closing brace
            j = i + 1
            while j < len(template) and template[j] != '}':
                j += 1
            
            if j < len(template):
                key = template[i + 1:j]
                if key in kwargs:
                    result.append(str(kwargs[key]))
                else:
                    result.append(template[i:j + 1])  # Keep original
                i = j + 1
            else:
                result.append(template[i])
                i += 1
        else:
            result.append(template[i])
            i += 1
    
    return ''.join(result)

# Using Python's built-in
def format_string_builtin(template, **kwargs):
    return template.format(**kwargs)

# Test
print(format_string("Hello {name}, you have {count} messages", name="Alice", count=5))
# "Hello Alice, you have 5 messages"
```

### Pattern Used: Template Processing

---

## Problem 10: Mask Sensitive Information

### Problem Statement
Mask sensitive information in strings (emails, phone numbers, credit cards).

### Solution
```python
def mask_email(email):
    """
    Mask email address.
    Time: O(n), Space: O(n)
    """
    if '@' not in email:
        return email
    
    local, domain = email.split('@', 1)
    
    # Mask local part
    if len(local) <= 2:
        masked_local = '*' * len(local)
    else:
        masked_local = local[0] + '*' * (len(local) - 2) + local[-1]
    
    return f"{masked_local}@{domain}"

def mask_phone(phone):
    """
    Mask phone number.
    Time: O(n), Space: O(n)
    """
    digits = ''.join(filter(str.isdigit, phone))
    
    if len(digits) == 10:
        return f"***-***-{digits[-4:]}"
    elif len(digits) == 11:
        return f"*-***-***-{digits[-4:]}"
    
    return phone

def mask_credit_card(card):
    """
    Mask credit card number.
    Time: O(n), Space: O(n)
    """
    digits = ''.join(filter(str.isdigit, card))
    
    if len(digits) >= 4:
        return '*' * (len(digits) - 4) + digits[-4:]
    
    return card

# Test
print(mask_email("user@example.com"))  # "u***r@example.com"
print(mask_phone("123-456-7890"))  # "***-***-7890"
print(mask_credit_card("4532 1488 0343 6467"))  # "************6467"
```

### Pattern Used: Character Masking

---

## Summary: Part 7

### Patterns Covered:
1. **Regex Validation**: Email, phone, patterns
2. **Character Validation**: Check properties
3. **Formatting**: Currency, case conversion
4. **Template Processing**: Placeholder replacement
5. **Masking**: Hide sensitive information

### Key Takeaways:
- Regex: Powerful for pattern validation
- Character checks: Validate properties efficiently
- Formatting: Transform for display
- Masking: Protect sensitive data

---

**Next**: Part 8 will cover advanced string algorithms and optimizations.

