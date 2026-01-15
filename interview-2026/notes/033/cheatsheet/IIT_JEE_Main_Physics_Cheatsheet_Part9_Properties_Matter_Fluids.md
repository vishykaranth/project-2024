# IIT JEE Main Physics Cheatsheet - Part 9: Properties of Matter & Fluid Mechanics

## 📏 Elasticity

### Stress and Strain

**Stress:**
```
σ = F/A
```

**Types:**
- Normal stress: `σ = F/A`
- Shear stress: `τ = F/A`

**Strain:**
```
ε = ΔL/L (longitudinal)
γ = Δx/L (shear)
```

### Hooke's Law

**For wire/rod:**
```
F = YA(ΔL/L)
where Y = Young's modulus
```

**Young's modulus:**
```
Y = Stress/Strain = (F/A)/(ΔL/L) = FL/(AΔL)
```

**Bulk modulus:**
```
B = -V(dP/dV) = -ΔP/(ΔV/V)
```

**Shear modulus:**
```
G = τ/γ = (F/A)/(Δx/L) = FL/(AΔx)
```

**Relation:**
```
Y = 3B(1 - 2σ)
G = Y/[2(1 + σ)]
where σ = Poisson's ratio
```

**Poisson's ratio:**
```
σ = -(Δr/r)/(ΔL/L)
0 < σ < 0.5
```

### Energy Stored

**Elastic potential energy:**
```
U = ½ × Stress × Strain × Volume
U = ½FΔL = ½YA(ΔL)²/L
```

**Energy density:**
```
u = ½ × Stress × Strain
```

### Applications

**Wire elongation:**
```
ΔL = FL/(YA)
```

**Thermal stress:**
```
σ = YαΔT
F = YAαΔT
```

**Work done in stretching:**
```
W = ½FΔL = ½YA(ΔL)²/L
```

---

## 💧 Fluid Mechanics

### Pressure

**Definition:**
```
P = F/A
```

**Pressure in fluid:**
```
P = P₀ + ρgh
where P₀ = atmospheric pressure
```

**Gauge pressure:**
```
P_gauge = ρgh
```

**Absolute pressure:**
```
P_abs = P_atm + P_gauge
```

### Pascal's Law

**Statement:**
- Pressure applied to enclosed fluid is transmitted undiminished to all parts

**Hydraulic lift:**
```
F₂/F₁ = A₂/A₁
W₁ = W₂ (work done)
```

### Archimedes' Principle

**Buoyant force:**
```
F_b = ρ_fluid × V_displaced × g
F_b = Weight of fluid displaced
```

**Apparent weight:**
```
W_apparent = W_actual - F_b
```

**Floating condition:**
```
ρ_body < ρ_fluid (floats)
ρ_body = ρ_fluid (neutral)
ρ_body > ρ_fluid (sinks)
```

**Fraction submerged:**
```
V_submerged/V_total = ρ_body/ρ_fluid
```

### Continuity Equation

**For incompressible fluid:**
```
A₁v₁ = A₂v₂ = constant
```

**Volume flow rate:**
```
Q = Av = constant
```

### Bernoulli's Principle

**Bernoulli's equation:**
```
P + ½ρv² + ρgh = constant
P₁ + ½ρv₁² + ρgh₁ = P₂ + ½ρv₂² + ρgh₂
```

**Applications:**

**Venturi meter:**
```
v₂ = A₁√[2(P₁-P₂)/ρ(A₁²-A₂²)]
```

**Torricelli's theorem:**
```
v = √(2gh)
```

**Pitot tube:**
```
v = √(2(P_static - P_dynamic)/ρ)
```

### Viscosity

**Coefficient of viscosity:**
```
F = ηA(dv/dx)
F = ηAv/d (for parallel plates)
```

**Poiseuille's equation:**
```
Q = πr⁴(P₁-P₂)/(8ηL)
```

**Stokes' law:**
```
F = 6πηrv
```

**Terminal velocity:**
```
v_t = 2r²(ρ - σ)g/(9η)
where ρ = density of sphere, σ = density of fluid
```

### Surface Tension

**Definition:**
```
T = F/l
```

**Surface energy:**
```
E = T × ΔA
```

**Excess pressure:**

**For spherical drop:**
```
P_excess = 2T/r
```

**For soap bubble:**
```
P_excess = 4T/r
```

**Capillary rise:**
```
h = 2T cos θ/(ρgr)
```

**For water (θ ≈ 0°):**
```
h = 2T/(ρgr)
```

### Reynold's Number

```
Re = ρvD/η
```

**Flow types:**
- Laminar: `Re < 2000`
- Turbulent: `Re > 3000`
- Transition: `2000 < Re < 3000`

---

## 🌡️ Thermal Properties

### Thermal Expansion

**Linear expansion:**
```
L = L₀(1 + αΔT)
ΔL = L₀αΔT
where α = coefficient of linear expansion
```

**Area expansion:**
```
A = A₀(1 + βΔT)
β = 2α (approximately)
```

**Volume expansion:**
```
V = V₀(1 + γΔT)
γ = 3α (approximately)
```

**For liquids:**
```
γ_apparent = γ_liquid - 3α_container
```

### Calorimetry

**Heat transfer:**
```
Q = mcΔT
Q = mL (for phase change)
```

**Heat capacity:**
```
C = Q/ΔT = mc
```

**Specific heat:**
```
c = Q/(mΔT)
```

**Latent heat:**
```
L = Q/m
```

**Principle of calorimetry:**
```
Heat lost = Heat gained
m₁c₁(T₁ - T_f) = m₂c₂(T_f - T₂)
```

### Heat Transfer

**Conduction:**
```
Q/t = -kA(dT/dx)
Q/t = kA(T₁ - T₂)/L (for steady state)
```

**Thermal resistance:**
```
R = L/(kA)
```

**Series combination:**
```
R_eq = R₁ + R₂ + ...
```

**Parallel combination:**
```
1/R_eq = 1/R₁ + 1/R₂ + ...
```

**Convection:**
```
Q/t = hAΔT
where h = convection coefficient
```

**Radiation:**
```
Stefan's law: P = σAeT⁴
where σ = 5.67 × 10⁻⁸ W/m²K⁴
e = emissivity (0 ≤ e ≤ 1)
```

**Wien's displacement law:**
```
λ_max T = constant = 2.9 × 10⁻³ m·K
```

**Newton's law of cooling:**
```
dT/dt = -k(T - T_surrounding)
T = T_s + (T₀ - T_s)e^(-kt)
```

---

## 📊 Quick Reference

### Constants
- `g = 9.8 m/s²`
- `P_atm = 1.013 × 10⁵ Pa`
- `σ = 5.67 × 10⁻⁸ W/m²K⁴`
- `Wien's constant = 2.9 × 10⁻³ m·K`

### Units
- Pressure: Pa (Pascal) = N/m²
- Viscosity: Pa·s = N·s/m²
- Surface tension: N/m
- Specific heat: J/(kg·K)
- Latent heat: J/kg

### Important Relations
- `P = P₀ + ρgh`
- `F_b = ρVg`
- `A₁v₁ = A₂v₂`
- `P + ½ρv² + ρgh = constant`
- `ΔL = L₀αΔT`
- `Q = mcΔT`

### Common Mistakes
1. Confusing gauge and absolute pressure
2. Wrong sign in Bernoulli's equation
3. Not considering viscosity in flow problems
4. Confusing linear, area, and volume expansion
5. Forgetting latent heat in phase changes

---

**Next: Part 10 - Quick Reference Formulas & Constants**

