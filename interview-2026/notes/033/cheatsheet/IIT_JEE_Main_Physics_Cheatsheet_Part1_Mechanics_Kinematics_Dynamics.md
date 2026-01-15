# IIT JEE Main Physics Cheatsheet - Part 1: Mechanics (Kinematics & Dynamics)

## 📐 Kinematics

### Basic Equations of Motion

**For constant acceleration:**

```
v = u + at
s = ut + ½at²
v² = u² + 2as
s = (u + v)t/2
```

**Where:**
- `u` = initial velocity
- `v` = final velocity
- `a` = acceleration
- `s` = displacement
- `t` = time

### Projectile Motion

**Horizontal Motion:**
```
x = uₓt = u cos θ · t
uₓ = u cos θ (constant)
```

**Vertical Motion:**
```
y = uᵧt - ½gt² = u sin θ · t - ½gt²
vᵧ = u sin θ - gt
uᵧ = u sin θ
```

**Time of Flight:**
```
T = 2u sin θ / g
```

**Maximum Height:**
```
H = u² sin²θ / 2g
```

**Range:**
```
R = u² sin 2θ / g
R_max = u² / g (when θ = 45°)
```

**Range for same speed, complementary angles:**
```
R₁ = R₂ if θ₁ + θ₂ = 90°
```

### Relative Velocity

**Relative velocity of A w.r.t. B:**
```
v⃗_AB = v⃗_A - v⃗_B
```

**For two objects moving in same direction:**
```
v_rel = |v₁ - v₂|
```

**For two objects moving in opposite directions:**
```
v_rel = v₁ + v₂
```

### Circular Motion

**Angular displacement:**
```
θ = s/r (radians)
```

**Angular velocity:**
```
ω = dθ/dt = v/r
ω = 2π/T = 2πf
```

**Angular acceleration:**
```
α = dω/dt = a_t/r
```

**Linear and angular relations:**
```
v = rω
a_t = rα (tangential)
a_c = v²/r = rω² (centripetal)
```

**Total acceleration:**
```
a = √(a_t² + a_c²)
```

**Centripetal force:**
```
F_c = mv²/r = mrω²
```

---

## ⚖️ Dynamics

### Newton's Laws of Motion

**First Law (Law of Inertia):**
- Body at rest stays at rest, body in motion stays in motion unless acted upon by external force
- `ΣF = 0` → `a = 0`

**Second Law:**
```
F⃗ = ma⃗
F = dp/dt (where p = momentum)
```

**Third Law:**
```
F⃗_AB = -F⃗_BA
Action and reaction are equal and opposite
```

### Friction

**Static Friction:**
```
f_s ≤ μ_s N
f_s_max = μ_s N
```

**Kinetic Friction:**
```
f_k = μ_k N
```

**Angle of Friction:**
```
tan λ = μ
```

**Angle of Repose:**
```
tan θ = μ
```

**Rolling Friction:**
```
f_r = μ_r N (μ_r < μ_k < μ_s)
```

### Tension in Strings

**For massless, inextensible string:**
- Tension is same throughout
- For pulley systems, use constraint equations

**For string with mass:**
- Tension varies along length

### Constraint Relations

**For connected bodies:**
- Length of string remains constant
- Use differentiation to find velocity/acceleration relations

**Example (Pulley system):**
```
If x₁ + x₂ = constant
Then v₁ + v₂ = 0
And a₁ + a₂ = 0
```

### Pseudo Force

**In non-inertial frame:**
```
F_pseudo = -ma_frame
```

**For rotating frame:**
```
F_centrifugal = mω²r (outward)
F_coriolis = 2m(v⃗ × ω⃗)
```

---

## 🎯 Important Concepts

### Free Body Diagram (FBD)
- Draw all forces acting on the body
- Resolve forces into components
- Apply Newton's laws

### Equilibrium
**For equilibrium:**
```
ΣF_x = 0
ΣF_y = 0
ΣF_z = 0
```

### Inclined Plane

**For block on inclined plane (angle θ):**
```
Component along plane: mg sin θ
Component perpendicular: mg cos θ
Normal force: N = mg cos θ
Acceleration: a = g sin θ (if no friction)
```

**With friction:**
```
a = g(sin θ - μ cos θ) (downward)
a = g(sin θ + μ cos θ) (upward)
```

### Atwood's Machine

**For two masses m₁ and m₂ (m₁ > m₂):**
```
Acceleration: a = (m₁ - m₂)g / (m₁ + m₂)
Tension: T = 2m₁m₂g / (m₁ + m₂)
```

### Spring Force

**Hooke's Law:**
```
F = -kx
```

**Spring constant in series:**
```
1/k_eq = 1/k₁ + 1/k₂ + ...
```

**Spring constant in parallel:**
```
k_eq = k₁ + k₂ + ...
```

### Momentum

**Linear Momentum:**
```
p⃗ = mv⃗
```

**Conservation of Momentum:**
```
If ΣF_external = 0, then Σp = constant
```

**Impulse:**
```
J⃗ = F⃗_avg × Δt = Δp⃗
```

### Collisions

**Elastic Collision:**
- Momentum conserved: `m₁u₁ + m₂u₂ = m₁v₁ + m₂v₂`
- Kinetic energy conserved: `½m₁u₁² + ½m₂u₂² = ½m₁v₁² + ½m₂v₂²`

**Coefficient of Restitution:**
```
e = (v₂ - v₁) / (u₁ - u₂)
e = 1 (elastic), e = 0 (perfectly inelastic), 0 < e < 1 (inelastic)
```

**For head-on elastic collision:**
```
v₁ = [(m₁ - m₂)u₁ + 2m₂u₂] / (m₁ + m₂)
v₂ = [(m₂ - m₁)u₂ + 2m₁u₁] / (m₁ + m₂)
```

**Special cases:**
- If m₁ = m₂: `v₁ = u₂, v₂ = u₁` (velocities exchange)
- If m₂ >> m₁: `v₁ ≈ -u₁ + 2u₂, v₂ ≈ u₂`
- If m₁ >> m₂: `v₁ ≈ u₁, v₂ ≈ 2u₁ - u₂`

---

## 📊 Quick Reference

### Units
- Force: N (Newton) = kg·m/s²
- Momentum: kg·m/s
- Impulse: N·s = kg·m/s

### Important Values
- g = 9.8 m/s² (standard)
- g = 10 m/s² (approximation)

### Sign Conventions
- Upward/Right: +ve
- Downward/Left: -ve
- Acceleration due to gravity: -g (if upward is +ve)

### Common Mistakes to Avoid
1. Confusing distance and displacement
2. Using v = u + at when acceleration is not constant
3. Forgetting to resolve forces into components
4. Not considering all forces in FBD
5. Confusing static and kinetic friction

---

**Next: Part 2 - Work, Energy, Power & Rotational Motion**

