# IIT JEE Main Physics Cheatsheet - Part 5: Electrostatics & Current Electricity

## ⚡ Electrostatics

### Coulomb's Law

**Force between two charges:**
```
F = (1/4πε₀) × (q₁q₂/r²) = kq₁q₂/r²
```

**Where:**
- `k = 1/4πε₀ = 9 × 10⁹ N·m²/C²`
- `ε₀ = 8.85 × 10⁻¹² C²/N·m²` (permittivity of free space)

**Vector form:**
```
F⃗₁₂ = (kq₁q₂/r²) r̂₁₂
```

**For multiple charges:**
```
F⃗ = Σ F⃗ᵢ (superposition principle)
```

### Electric Field

**Definition:**
```
E⃗ = F⃗/q₀
```

**Due to point charge:**
```
E = kq/r² (radially outward for +q)
E⃗ = (kq/r²) r̂
```

**Due to multiple charges:**
```
E⃗ = Σ E⃗ᵢ
```

**Due to dipole:**
```
On axis: E = (2kp)/r³ (along dipole)
On perpendicular bisector: E = kp/r³ (opposite to dipole)
At general point: E = (kp/r³)√(3cos²θ + 1)
```

**Electric field intensity:**
```
E = F/q
```

### Electric Potential

**Definition:**
```
V = U/q = W/q
```

**Due to point charge:**
```
V = kq/r
```

**Due to multiple charges:**
```
V = Σ Vᵢ = Σ kqᵢ/rᵢ
```

**Due to dipole:**
```
On axis: V = kp/r²
On perpendicular bisector: V = 0
At general point: V = (kp cos θ)/r²
```

**Potential difference:**
```
V_B - V_A = -∫ E⃗ · dl⃗
```

**Relation with field:**
```
E = -dV/dr
E_x = -∂V/∂x
E_y = -∂V/∂y
E_z = -∂V/∂z
```

### Electric Potential Energy

**For two charges:**
```
U = kq₁q₂/r
```

**For system of charges:**
```
U = (1/2) Σᵢ Σⱼ (kqᵢqⱼ/rᵢⱼ) (i ≠ j)
```

**Work done:**
```
W = q(V_B - V_A) = ΔU
```

### Gauss's Law

**Statement:**
```
∮ E⃗ · dA⃗ = Q_enclosed/ε₀
```

**Applications:**

**1. Uniformly charged sphere:**
```
Inside (r < R): E = (kQr)/R³
Surface (r = R): E = kQ/R²
Outside (r > R): E = kQ/r²
```

**2. Infinite plane sheet:**
```
E = σ/2ε₀ (on both sides)
where σ = surface charge density
```

**3. Infinite line charge:**
```
E = λ/(2πε₀r) = 2kλ/r
where λ = linear charge density
```

**4. Charged conductor:**
```
Inside: E = 0
Surface: E = σ/ε₀ (perpendicular to surface)
Outside: E = kQ/r²
```

### Capacitance

**Definition:**
```
C = Q/V
```

**Parallel plate capacitor:**
```
C = ε₀A/d
With dielectric: C = Kε₀A/d = KC₀
```

**Spherical capacitor:**
```
C = 4πε₀ab/(b-a)
where a, b = inner and outer radii
```

**Cylindrical capacitor:**
```
C = 2πε₀L/ln(b/a)
```

**Isolated sphere:**
```
C = 4πε₀R
```

**Capacitors in series:**
```
1/C_eq = 1/C₁ + 1/C₂ + ...
Q same, V divides
```

**Capacitors in parallel:**
```
C_eq = C₁ + C₂ + ...
V same, Q divides
```

### Energy Stored in Capacitor

```
U = ½QV = ½CV² = Q²/2C
```

**Energy density:**
```
u = ½ε₀E² = ½DE
where D = ε₀E (electric displacement)
```

### Dielectrics

**Dielectric constant:**
```
K = ε/ε₀ = C/C₀
```

**Polarization:**
```
P = ε₀χE
where χ = electric susceptibility
```

**Relation:**
```
K = 1 + χ
```

---

## 🔌 Current Electricity

### Current

**Definition:**
```
I = dQ/dt = nAve
```

**Where:**
- `n` = number density of charge carriers
- `A` = cross-sectional area
- `v` = drift velocity
- `e` = charge on electron

**Drift velocity:**
```
v_d = eEτ/m = (e/m)(V/l)τ
where τ = relaxation time
```

**Current density:**
```
J = I/A = σE = ne²Eτ/m
where σ = conductivity
```

### Resistance

**Ohm's Law:**
```
V = IR
```

**Resistance:**
```
R = ρl/A = l/(σA)
where ρ = resistivity, σ = conductivity
```

**Resistivity:**
```
ρ = 1/σ = m/(ne²τ)
```

**Temperature dependence:**
```
R = R₀[1 + α(T - T₀)]
ρ = ρ₀[1 + α(T - T₀)]
where α = temperature coefficient
```

**Resistors in series:**
```
R_eq = R₁ + R₂ + ...
I same, V divides
```

**Resistors in parallel:**
```
1/R_eq = 1/R₁ + 1/R₂ + ...
V same, I divides
```

### Power and Energy

**Power:**
```
P = VI = I²R = V²/R
```

**Energy:**
```
W = VIt = I²Rt = V²t/R
```

**Heat generated (Joule's law):**
```
H = I²Rt = VIt = V²t/R
```

### EMF and Internal Resistance

**EMF:**
```
ε = W/q
```

**Terminal voltage:**
```
V = ε - Ir
where r = internal resistance
```

**Current:**
```
I = ε/(R + r)
```

**Power delivered:**
```
P = I²R = ε²R/(R + r)²
```

**Maximum power:**
```
P_max when R = r
P_max = ε²/4r
```

### Kirchhoff's Laws

**First Law (Junction Rule):**
```
ΣI_in = ΣI_out
ΣI = 0 at junction
```

**Second Law (Loop Rule):**
```
Σε = ΣIR
ΣV = 0 in closed loop
```

### RC Circuits

**Charging:**
```
q = Q₀(1 - e^(-t/RC))
I = (ε/R)e^(-t/RC)
V_C = ε(1 - e^(-t/RC))
```

**Discharging:**
```
q = Q₀e^(-t/RC)
I = -(Q₀/RC)e^(-t/RC)
V_C = V₀e^(-t/RC)
```

**Time constant:**
```
τ = RC
```

### Wheatstone Bridge

**Balanced condition:**
```
R₁/R₂ = R₃/R₄
or
R₁R₄ = R₂R₃
```

**Unbalanced:**
```
I_g = ε(R₂R₃ - R₁R₄)/[R_g(R₁+R₂)(R₃+R₄) + R₁R₂(R₃+R₄) + R₃R₄(R₁+R₂)]
```

### Meter Bridge

**Resistance:**
```
R/X = l/(100 - l)
where l = balancing length
```

### Potentiometer

**EMF comparison:**
```
ε₁/ε₂ = l₁/l₂
```

**Internal resistance:**
```
r = R(l₁ - l₂)/l₂
```

---

## 📊 Quick Reference

### Constants
- `k = 9 × 10⁹ N·m²/C²`
- `ε₀ = 8.85 × 10⁻¹² C²/N·m²`
- `e = 1.6 × 10⁻¹⁹ C`
- `m_e = 9.1 × 10⁻³¹ kg`

### Units
- Charge: C (Coulomb)
- Electric field: N/C or V/m
- Potential: V (Volt) = J/C
- Capacitance: F (Farad) = C/V
- Current: A (Ampere) = C/s
- Resistance: Ω (Ohm) = V/A
- Resistivity: Ω·m
- Power: W (Watt) = J/s

### Important Relations
- `E = -dV/dr`
- `F = qE`
- `U = qV`
- `C = Q/V`
- `V = IR`
- `P = VI = I²R`

### Common Mistakes
1. Confusing electric field with electric potential
2. Wrong sign in potential difference
3. Forgetting internal resistance in battery problems
4. Confusing series and parallel combinations
5. Not applying Kirchhoff's laws correctly

---

**Next: Part 6 - Magnetism & Electromagnetic Induction**

