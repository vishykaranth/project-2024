# IIT JEE Main Physics Cheatsheet - Part 2: Work, Energy, Power & Rotational Motion

## 💪 Work, Energy & Power

### Work

**Definition:**
```
W = F⃗ · s⃗ = Fs cos θ
```

**For constant force:**
```
W = F_x · Δx + F_y · Δy + F_z · Δz
```

**Work done by variable force:**
```
W = ∫ F⃗ · ds⃗
```

**Work-Energy Theorem:**
```
W_total = ΔK = ½mv² - ½mu²
```

**Work done by different forces:**
- Gravity: `W = -mgΔh` (upward is +ve)
- Spring: `W = -½kx²` (from equilibrium)
- Friction: `W = -f_k · s` (always negative)
- Normal force: `W = 0` (perpendicular to displacement)

### Kinetic Energy

```
K = ½mv²
```

**For system of particles:**
```
K_total = Σ½mᵢvᵢ²
```

### Potential Energy

**Gravitational PE:**
```
U = mgh (h measured from reference)
U = -GMm/r (for planetary motion)
```

**Spring PE:**
```
U = ½kx²
```

**Elastic PE:**
```
U = ½ × Stress × Strain × Volume
```

### Conservation of Energy

**Mechanical Energy:**
```
E = K + U
```

**If only conservative forces:**
```
E_initial = E_final
K₁ + U₁ = K₂ + U₂
```

**With non-conservative forces:**
```
W_nc = ΔE = ΔK + ΔU
```

### Power

**Average Power:**
```
P_avg = W/t = ΔE/t
```

**Instantaneous Power:**
```
P = dW/dt = F⃗ · v⃗ = Fv cos θ
```

**For rotating body:**
```
P = τω
```

**Efficiency:**
```
η = P_out / P_in × 100%
```

---

## 🔄 Rotational Motion

### Rotational Kinematics

**Angular displacement:**
```
θ = θ₀ + ω₀t + ½αt²
```

**Angular velocity:**
```
ω = ω₀ + αt
```

**Angular acceleration:**
```
α = dω/dt = d²θ/dt²
```

**Relation:**
```
ω² = ω₀² + 2αθ
```

**Linear-angular relations:**
```
s = rθ
v = rω
a_t = rα
a_c = rω² = v²/r
```

### Moment of Inertia

**Definition:**
```
I = Σmᵢrᵢ² = ∫ r²dm
```

**Parallel Axis Theorem:**
```
I = I_cm + Md²
```

**Perpendicular Axis Theorem (for lamina):**
```
I_z = I_x + I_y
```

**Common Moments of Inertia:**

| Object | Axis | Moment of Inertia |
|--------|------|-------------------|
| Rod (length L) | Through center, ⊥ | ML²/12 |
| Rod (length L) | Through end, ⊥ | ML²/3 |
| Ring (radius R) | Through center, ⊥ | MR² |
| Disc (radius R) | Through center, ⊥ | MR²/2 |
| Solid sphere (radius R) | Through center | 2MR²/5 |
| Hollow sphere (radius R) | Through center | 2MR²/3 |
| Cylinder (radius R) | Central axis | MR²/2 |
| Cylinder (radius R) | Perpendicular to axis | M(R²/4 + L²/12) |

### Torque

**Definition:**
```
τ⃗ = r⃗ × F⃗
τ = rF sin θ = F × (perpendicular distance)
```

**Rotational Newton's Second Law:**
```
τ = Iα
Στ = Iα
```

### Angular Momentum

**Definition:**
```
L⃗ = r⃗ × p⃗ = r⃗ × mv⃗
L = mvr sin θ
```

**For rotation about fixed axis:**
```
L = Iω
```

**Conservation of Angular Momentum:**
```
If Στ_external = 0, then L = constant
I₁ω₁ = I₂ω₂
```

**Rate of change:**
```
dL/dt = τ
```

### Rotational Kinetic Energy

```
K_rot = ½Iω²
```

**Total kinetic energy (rolling):**
```
K_total = K_trans + K_rot = ½mv² + ½Iω²
```

**For pure rolling (v = rω):**
```
K = ½mv²(1 + k²/R²)
where k = radius of gyration
```

### Rolling Motion

**Condition for pure rolling:**
```
v = rω (no slipping)
```

**Acceleration on inclined plane:**
```
a = g sin θ / (1 + I/mr²)
```

**For different objects on same incline:**
- Solid sphere: `a = (5/7)g sin θ`
- Hollow sphere: `a = (3/5)g sin θ`
- Solid cylinder: `a = (2/3)g sin θ`
- Ring: `a = (1/2)g sin θ`

**Minimum friction for rolling:**
```
f = Iα/r = (I/mr²) × mg sin θ / (1 + I/mr²)
```

### Rotational Work and Power

**Work done by torque:**
```
W = ∫ τ dθ = τθ (if constant)
```

**Power:**
```
P = τω
```

---

## 🎯 Important Concepts

### Center of Mass

**Position:**
```
r_cm = (Σmᵢrᵢ) / (Σmᵢ)
```

**For 2D:**
```
x_cm = (m₁x₁ + m₂x₂) / (m₁ + m₂)
y_cm = (m₁y₁ + m₂y₂) / (m₁ + m₂)
```

**Velocity:**
```
v_cm = (Σmᵢvᵢ) / (Σmᵢ)
```

**Acceleration:**
```
a_cm = (Σmᵢaᵢ) / (Σmᵢ) = F_net / M_total
```

**Momentum:**
```
p_cm = M_total × v_cm
```

### Collision in Rotational Frame

**Angular impulse:**
```
J_angular = τ × Δt = ΔL
```

### Gyroscope

**Precession:**
```
Ω = τ / (Iω)
```

---

## 📊 Quick Reference

### Energy Units
- Work/Energy: J (Joule) = N·m = kg·m²/s²
- Power: W (Watt) = J/s

### Rotational Units
- Angular displacement: rad
- Angular velocity: rad/s
- Angular acceleration: rad/s²
- Moment of inertia: kg·m²
- Torque: N·m
- Angular momentum: kg·m²/s

### Important Relations
- `K_rot / K_trans = I/(mr²)` for rolling
- `v_cm = rω` for pure rolling
- `L = Iω` for fixed axis rotation

### Common Mistakes
1. Confusing moment of inertia with mass
2. Forgetting to include rotational KE in energy conservation
3. Not considering rolling friction
4. Confusing torque with force
5. Not applying parallel axis theorem correctly

---

**Next: Part 3 - Thermodynamics & Kinetic Theory**

