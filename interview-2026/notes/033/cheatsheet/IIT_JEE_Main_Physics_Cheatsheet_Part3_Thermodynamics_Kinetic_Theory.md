# IIT JEE Main Physics Cheatsheet - Part 3: Thermodynamics & Kinetic Theory

## 🌡️ Thermodynamics

### Laws of Thermodynamics

**Zeroth Law:**
- If A is in thermal equilibrium with B, and B with C, then A is in thermal equilibrium with C
- Defines temperature

**First Law:**
```
ΔU = Q - W
or
Q = ΔU + W
```

**Where:**
- `Q` = heat added to system (+ve if added, -ve if removed)
- `W` = work done by system (+ve if system does work, -ve if work done on system)
- `ΔU` = change in internal energy

**Sign Convention:**
- Heat added: +Q
- Heat removed: -Q
- Work done by system: +W
- Work done on system: -W

**Second Law:**
- Heat cannot flow from colder to hotter body spontaneously
- Entropy of isolated system always increases
- No engine can be 100% efficient

**Third Law:**
- Entropy approaches zero as temperature approaches absolute zero

### Work Done by Gas

**General:**
```
W = ∫ P dV
```

**Constant pressure (Isobaric):**
```
W = PΔV = P(V₂ - V₁)
```

**Constant volume (Isochoric):**
```
W = 0
```

**Isothermal process:**
```
W = nRT ln(V₂/V₁) = nRT ln(P₁/P₂)
```

**Adiabatic process:**
```
W = (P₁V₁ - P₂V₂) / (γ - 1)
W = nR(T₁ - T₂) / (γ - 1)
```

### Heat Capacity

**Molar heat capacity at constant volume:**
```
C_V = (ΔQ/nΔT)_V = (dU/dT)_V
```

**Molar heat capacity at constant pressure:**
```
C_P = (ΔQ/nΔT)_P
```

**Relation:**
```
C_P - C_V = R
C_P / C_V = γ
```

**For monatomic gas:**
```
C_V = (3/2)R
C_P = (5/2)R
γ = 5/3
```

**For diatomic gas:**
```
C_V = (5/2)R
C_P = (7/2)R
γ = 7/5
```

**For polyatomic gas:**
```
C_V = 3R
C_P = 4R
γ = 4/3
```

### Thermodynamic Processes

**Isothermal (T = constant):**
```
PV = constant
W = nRT ln(V₂/V₁)
Q = W (since ΔU = 0)
ΔU = 0
```

**Isobaric (P = constant):**
```
V/T = constant
W = PΔV
Q = nC_PΔT
ΔU = nC_VΔT
```

**Isochoric (V = constant):**
```
P/T = constant
W = 0
Q = nC_VΔT = ΔU
ΔU = nC_VΔT
```

**Adiabatic (Q = 0):**
```
PV^γ = constant
TV^(γ-1) = constant
P^(1-γ) T^γ = constant
W = -ΔU
Q = 0
ΔU = nC_VΔT
```

### Heat Engines

**Efficiency:**
```
η = W/Q_H = 1 - Q_C/Q_H = 1 - T_C/T_H (Carnot)
```

**Carnot Engine:**
```
η_max = 1 - T_C/T_H
```

**Coefficient of Performance (Refrigerator):**
```
COP = Q_C/W = T_C/(T_H - T_C)
```

**Coefficient of Performance (Heat Pump):**
```
COP = Q_H/W = T_H/(T_H - T_C)
```

### Entropy

**Definition:**
```
ΔS = ∫ dQ_rev / T
```

**For reversible process:**
```
ΔS = Q_rev / T
```

**For isothermal process:**
```
ΔS = nR ln(V₂/V₁) = nR ln(P₁/P₂)
```

**For adiabatic process:**
```
ΔS = 0 (reversible adiabatic)
```

**Entropy change for ideal gas:**
```
ΔS = nC_V ln(T₂/T₁) + nR ln(V₂/V₁)
ΔS = nC_P ln(T₂/T₁) - nR ln(P₂/P₁)
```

---

## ⚛️ Kinetic Theory of Gases

### Ideal Gas Equation

**General:**
```
PV = nRT = NkT
```

**Where:**
- `n` = number of moles
- `N` = number of molecules
- `R` = universal gas constant = 8.314 J/mol·K
- `k` = Boltzmann constant = 1.38 × 10⁻²³ J/K
- `R = N_A × k` (N_A = Avogadro's number)

**Different forms:**
```
PV = (m/M)RT (m = mass, M = molar mass)
P = ρRT/M (ρ = density)
PV = NkT
```

### Kinetic Theory Assumptions

1. Gas consists of large number of molecules
2. Molecules are point masses
3. Molecules are in random motion
4. Collisions are elastic
5. No intermolecular forces (except during collision)
6. Volume of molecules << volume of container

### Pressure of Ideal Gas

**From kinetic theory:**
```
P = (1/3)ρv_rms² = (1/3)(N/V)mv_rms²
P = (1/3)(N/V)m⟨v²⟩
```

**RMS Speed:**
```
v_rms = √(⟨v²⟩) = √(3RT/M) = √(3kT/m)
```

**Average Speed:**
```
v_avg = √(8RT/πM) = √(8kT/πm)
```

**Most Probable Speed:**
```
v_mp = √(2RT/M) = √(2kT/m)
```

**Relation:**
```
v_mp : v_avg : v_rms = 1 : 1.128 : 1.224
v_mp : v_avg : v_rms = √2 : √(8/π) : √3
```

### Kinetic Energy

**Average translational KE per molecule:**
```
⟨KE⟩ = (3/2)kT = (1/2)mv_rms²
```

**Total KE for N molecules:**
```
KE_total = (3/2)NkT = (3/2)nRT
```

**For monatomic gas:**
```
U = (3/2)nRT = KE_total
```

**For diatomic gas (at room temp):**
```
U = (5/2)nRT (includes rotational)
```

**For polyatomic gas:**
```
U = 3nRT (includes rotational and vibrational)
```

### Maxwell-Boltzmann Distribution

**Distribution function:**
```
f(v) = 4πn(m/2πkT)^(3/2) v² e^(-mv²/2kT)
```

**Most probable speed:**
```
v_mp = √(2kT/m) = √(2RT/M)
```

### Mean Free Path

**Definition:**
```
λ = 1/(√2 πd²n) = kT/(√2 πd²P)
```

**Where:**
- `d` = molecular diameter
- `n` = number density

### Degrees of Freedom

**Monatomic gas:**
```
f = 3 (translational only)
```

**Diatomic gas (at room temp):**
```
f = 5 (3 translational + 2 rotational)
```

**Diatomic gas (at high temp):**
```
f = 7 (3 translational + 2 rotational + 2 vibrational)
```

**Polyatomic gas:**
```
f = 6 (3 translational + 3 rotational)
```

**Equipartition Theorem:**
```
Energy per degree of freedom = (1/2)kT
U = (f/2)nRT
C_V = (f/2)R
C_P = (f/2 + 1)R = (f+2)R/2
γ = 1 + 2/f
```

### Real Gases

**Van der Waals Equation:**
```
(P + an²/V²)(V - nb) = nRT
```

**Where:**
- `a` = correction for intermolecular forces
- `b` = correction for molecular volume

**Critical Constants:**
```
V_c = 3nb
P_c = a/(27b²)
T_c = 8a/(27Rb)
```

**Reduced variables:**
```
P_r = P/P_c
V_r = V/V_c
T_r = T/T_c
```

---

## 📊 Quick Reference

### Constants
- R = 8.314 J/mol·K
- k = 1.38 × 10⁻²³ J/K
- N_A = 6.022 × 10²³ mol⁻¹
- Standard temperature: 273.15 K = 0°C
- Standard pressure: 1 atm = 1.013 × 10⁵ Pa

### Important Relations
- `PV = nRT` (ideal gas)
- `v_rms = √(3RT/M)`
- `⟨KE⟩ = (3/2)kT`
- `C_P - C_V = R`
- `γ = C_P/C_V = 1 + 2/f`

### Process Summary

| Process | Condition | PV relation | Work | Heat | ΔU |
|---------|-----------|-------------|------|------|-----|
| Isothermal | T = const | PV = const | nRT ln(V₂/V₁) | = W | 0 |
| Isobaric | P = const | V/T = const | PΔV | nC_PΔT | nC_VΔT |
| Isochoric | V = const | P/T = const | 0 | nC_VΔT | nC_VΔT |
| Adiabatic | Q = 0 | PV^γ = const | -nC_VΔT | 0 | nC_VΔT |

### Common Mistakes
1. Confusing work done by gas vs on gas
2. Wrong sign convention for Q and W
3. Using wrong heat capacity (C_V vs C_P)
4. Forgetting that ΔU = 0 for isothermal process
5. Confusing v_rms, v_avg, and v_mp

---

**Next: Part 4 - Waves & Oscillations**

