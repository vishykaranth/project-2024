# IIT JEE Main Physics Cheatsheet - Part 8: Modern Physics

## ⚛️ Atomic Physics

### Photoelectric Effect

**Einstein's equation:**
```
hν = φ + K_max
hν = hν₀ + ½mv²_max
```

**Where:**
- `h = 6.63 × 10⁻³⁴ J·s` (Planck's constant)
- `ν` = frequency of incident light
- `ν₀` = threshold frequency
- `φ = hν₀` = work function
- `K_max` = maximum kinetic energy

**Threshold frequency:**
```
ν₀ = φ/h
```

**Stopping potential:**
```
eV₀ = K_max = hν - φ
V₀ = (h/e)(ν - ν₀)
```

**Photocurrent:**
- Proportional to intensity
- Independent of frequency (if ν > ν₀)

**Cut-off potential:**
```
V₀ = (h/e)(ν - ν₀)
```

### Bohr's Model

**Angular momentum quantization:**
```
mvr = nh/2π = nℏ
where ℏ = h/2π
```

**Radius of orbit:**
```
r_n = n²a₀ = n²(ε₀h²)/(πme²)
a₀ = 0.529 Å (Bohr radius)
```

**Velocity:**
```
v_n = e²/(2ε₀nh) = v₁/n
v₁ = 2.18 × 10⁶ m/s
```

**Energy:**
```
E_n = -13.6/n² eV = -Rhc/n²
E_n = -2.18 × 10⁻¹⁸/n² J
```

**Energy levels:**
```
E₁ = -13.6 eV (ground state)
E₂ = -3.4 eV
E₃ = -1.51 eV
E₄ = -0.85 eV
E_∞ = 0 (ionization)
```

**Rydberg formula:**
```
1/λ = R(1/n₁² - 1/n₂²)
R = 1.097 × 10⁷ m⁻¹ (Rydberg constant)
```

**Spectral series:**
- Lyman: `n₁ = 1, n₂ = 2, 3, 4, ...` (UV)
- Balmer: `n₁ = 2, n₂ = 3, 4, 5, ...` (Visible)
- Paschen: `n₁ = 3, n₂ = 4, 5, 6, ...` (IR)
- Brackett: `n₁ = 4, n₂ = 5, 6, 7, ...` (IR)
- Pfund: `n₁ = 5, n₂ = 6, 7, 8, ...` (IR)

**Energy of photon:**
```
E = hν = hc/λ = 12400/λ(eV) (λ in Å)
```

**Ionization energy:**
```
E_ionization = |E_n| = 13.6/n² eV
```

### X-Rays

**Continuous spectrum (Bremsstrahlung):**
```
λ_min = hc/(eV) = 12400/V(Å)
where V = accelerating voltage
```

**Characteristic X-rays:**
```
Moseley's law: √ν = a(Z - b)
K_α line: ν = (3Rc/4)(Z - 1)²
```

**X-ray production:**
```
E = hν_max = eV
```

### de Broglie Wavelength

**Matter waves:**
```
λ = h/p = h/(mv)
λ = h/√(2mE) = h/√(2mqV)
```

**For electron:**
```
λ = 12.27/√V(Å) (V in volts)
```

**For photon:**
```
λ = hc/E
```

### Heisenberg Uncertainty Principle

**Position-Momentum:**
```
Δx · Δp ≥ ℏ/2
```

**Energy-Time:**
```
ΔE · Δt ≥ ℏ/2
```

---

## ☢️ Nuclear Physics

### Nuclear Properties

**Mass number (A):**
- Total number of nucleons (protons + neutrons)

**Atomic number (Z):**
- Number of protons

**Neutron number (N):**
```
N = A - Z
```

**Nuclear radius:**
```
R = R₀A^(1/3)
R₀ ≈ 1.2 × 10⁻¹⁵ m = 1.2 fm
```

**Nuclear density:**
```
ρ ≈ 2.3 × 10¹⁷ kg/m³ (constant for all nuclei)
```

### Binding Energy

**Mass defect:**
```
Δm = [Zm_p + (A-Z)m_n] - M_nucleus
```

**Binding energy:**
```
BE = Δmc²
BE = [Zm_p + (A-Z)m_n - M_nucleus]c²
```

**Binding energy per nucleon:**
```
BE/A = BE/A
Maximum at Fe-56 (~8.8 MeV/nucleon)
```

### Radioactivity

**Decay law:**
```
N = N₀e^(-λt)
A = A₀e^(-λt) = λN
```

**Half-life:**
```
T₁/₂ = ln(2)/λ = 0.693/λ
```

**Mean life:**
```
τ = 1/λ = T₁/₂/ln(2) = 1.44T₁/₂
```

**Activity:**
```
A = -dN/dt = λN = A₀e^(-λt)
```

**After n half-lives:**
```
N = N₀/2ⁿ
A = A₀/2ⁿ
```

### Alpha Decay

```
^A_ZX → ^(A-4)_(Z-2)Y + ^4_2He
```

**Energy released:**
```
Q = [M_X - M_Y - M_α]c²
```

**Range:**
```
R ∝ E^(3/2)
```

### Beta Decay

**Beta minus (β⁻):**
```
^A_ZX → ^A_(Z+1)Y + e⁻ + ν̄_e
```

**Beta plus (β⁺):**
```
^A_ZX → ^A_(Z-1)Y + e⁺ + ν_e
```

**Energy spectrum:**
- Continuous (due to neutrino)

### Gamma Decay

```
^A_ZX* → ^A_ZX + γ
```

**Energy:**
```
E_γ = E_excited - E_ground
```

### Nuclear Reactions

**Q-value:**
```
Q = (M_initial - M_final)c²
Q > 0: exothermic
Q < 0: endothermic
```

**Threshold energy:**
```
E_threshold = -Q(1 + m_a/m_A)
where m_a = projectile mass, m_A = target mass
```

### Nuclear Fission

**Example:**
```
^235_92U + n → ^236_92U* → ^141_56Ba + ^92_36Kr + 3n + Q
```

**Energy released:**
```
Q ≈ 200 MeV per fission
```

**Chain reaction:**
- Requires: `k ≥ 1` (multiplication factor)
- Critical mass required

### Nuclear Fusion

**Example:**
```
^2_1H + ^3_1H → ^4_2He + n + 17.6 MeV
```

**Requirements:**
- High temperature (~10⁷ K)
- High pressure
- Sufficient density

---

## 🔬 Electronics

### Semiconductors

**Intrinsic semiconductor:**
```
n_i = n_e = n_h = √(N_c N_v)e^(-E_g/2kT)
where E_g = band gap
```

**Extrinsic semiconductor:**
- n-type: Majority carriers = electrons
- p-type: Majority carriers = holes

**Carrier concentration:**
```
n_e n_h = n_i² (law of mass action)
```

### p-n Junction

**Depletion region:**
- Width depends on doping

**Built-in potential:**
```
V_bi = (kT/e)ln(N_a N_d/n_i²)
```

**Forward bias:**
- Current flows easily
- Barrier reduced

**Reverse bias:**
- Very small current (saturation current)
- Barrier increased

**Current:**
```
I = I₀(e^(eV/kT) - 1)
I₀ = reverse saturation current
```

### Diode

**I-V characteristic:**
- Forward: Exponential increase
- Reverse: Saturation current

**Breakdown:**
- Zener breakdown (low voltage)
- Avalanche breakdown (high voltage)

### Transistor

**Current relations:**
```
I_E = I_B + I_C
α = I_C/I_E (common base)
β = I_C/I_B = α/(1-α) (common emitter)
```

**Current gain:**
```
β = α/(1-α)
α = β/(1+β)
```

**Voltage gain:**
```
A_V = -βR_C/R_B
```

**Power gain:**
```
A_P = β²
```

### Logic Gates

**AND:**
```
Y = A · B
```

**OR:**
```
Y = A + B
```

**NOT:**
```
Y = Ā
```

**NAND:**
```
Y = A · B
```

**NOR:**
```
Y = A + B
```

**XOR:**
```
Y = A ⊕ B = AB̄ + ĀB
```

---

## 📊 Quick Reference

### Constants
- `h = 6.63 × 10⁻³⁴ J·s`
- `ℏ = h/2π = 1.05 × 10⁻³⁴ J·s`
- `c = 3 × 10⁸ m/s`
- `e = 1.6 × 10⁻¹⁹ C`
- `m_e = 9.1 × 10⁻³¹ kg`
- `m_p = 1.67 × 10⁻²⁷ kg`
- `1 eV = 1.6 × 10⁻¹⁹ J`
- `hc = 12400 eV·Å`

### Important Relations
- `E = hν = hc/λ`
- `λ = h/p = h/(mv)`
- `E_n = -13.6/n² eV`
- `N = N₀e^(-λt)`
- `T₁/₂ = 0.693/λ`
- `BE = Δmc²`

### Energy Conversions
- `1 eV = 1.6 × 10⁻¹⁹ J`
- `1 u = 931.5 MeV/c²`
- `hc = 12400 eV·Å`

### Common Mistakes
1. Confusing work function with threshold frequency
2. Wrong sign in energy levels
3. Confusing half-life with mean life
4. Not converting units (eV to J, etc.)
5. Confusing α, β, γ decay processes

---

**Next: Part 9 - Properties of Matter & Fluid Mechanics**

