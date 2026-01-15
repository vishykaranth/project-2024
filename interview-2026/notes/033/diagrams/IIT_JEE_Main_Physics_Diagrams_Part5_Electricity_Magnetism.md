# IIT JEE Main Physics - Complete Diagrams Guide (Part 5: Electricity & Magnetism)

## ⚡ Electricity and Magnetism

---

## 1. Electric Field and Potential

### Electric Field Lines
```
┌─────────────────────────────────────────────────────────────┐
│              Electric Field Lines                           │
└─────────────────────────────────────────────────────────────┘

Positive Charge:        Negative Charge:
    ╱│╲                    ╱│╲
   ╱ │ ╲                  ╱ │ ╲
  ╱  │  ╲                ╱  │  ╲
 ╱   │   ╲              ╱   │   ╲
╱    │    ╲            ╱    │    ╲
│    •    │            │    •    │
╲    │    ╱            ╲    │    ╱
 ╲   │   ╱              ╲   │   ╱
  ╲  │  ╱                ╲  │  ╱
   ╲ │ ╱                  ╲ │ ╱
    ╲│╱                    ╲│╱
    
Field lines:
- Start from positive, end at negative
- Never cross
- Density ∝ Field strength
```

### Electric Field
```
┌─────────────────────────────────────────────────────────────┐
│              Electric Field                                 │
└─────────────────────────────────────────────────────────────┘

Point Charge:
E = kq/r² = q/(4πε₀r²)
Direction: Away from +, towards -

Dipole:
E = (1/4πε₀)(2p/r³) (on axis)
E = (1/4πε₀)(p/r³) (perpendicular bisector)

Uniform Field:
E = V/d (between parallel plates)
```

### Electric Potential
```
┌─────────────────────────────────────────────────────────────┐
│              Electric Potential                             │
└─────────────────────────────────────────────────────────────┘

Point Charge:
V = kq/r = q/(4πε₀r)

Potential Difference:
ΔV = -∫E·dr

Work:
W = qΔV

Equipotential Surfaces:
- Perpendicular to field lines
- No work done along equipotential
```

---

## 2. Capacitors

### Parallel Plate Capacitor
```
┌─────────────────────────────────────────────────────────────┐
│              Parallel Plate Capacitor                        │
└─────────────────────────────────────────────────────────────┘

    ┌──────────┐
    │  +Q      │  ← Plate 1
    │          │
    │          │ d (separation)
    │          │
    │  -Q      │  ← Plate 2
    └──────────┘
    
Capacitance:
C = ε₀A/d

With dielectric:
C = Kε₀A/d
K = Dielectric constant

Energy:
U = ½CV² = ½Q²/C = ½QV
```

### Capacitor Combinations

#### Series
```
┌─────────────────────────────────────────────────────────────┐
│              Capacitors in Series                            │
└─────────────────────────────────────────────────────────────┘

    ────││───────││───────││────
    C₁      C₂      C₃
    
1/C_eq = 1/C₁ + 1/C₂ + 1/C₃
Q same for all
V = V₁ + V₂ + V₃
```

#### Parallel
```
┌─────────────────────────────────────────────────────────────┐
│              Capacitors in Parallel                          │
└─────────────────────────────────────────────────────────────┘

    ────││────
    │   C₁
    ├───││────
    │   C₂
    ├───││────
    │   C₃
    └─────────
    
C_eq = C₁ + C₂ + C₃
V same for all
Q = Q₁ + Q₂ + Q₃
```

---

## 3. Current Electricity

### Ohm's Law
```
┌─────────────────────────────────────────────────────────────┐
│              Ohm's Law                                       │
└─────────────────────────────────────────────────────────────┘

    ┌───┐
    │ R │  ← Resistor
    └───┘
      │
      │ I (current)
      │
      ▼
    
V = IR

Resistance:
R = ρL/A
ρ = Resistivity
```

### Resistor Combinations

#### Series
```
┌─────────────────────────────────────────────────────────────┐
│              Resistors in Series                             │
└─────────────────────────────────────────────────────────────┘

    ────[R₁]────[R₂]────[R₃]────
    
R_eq = R₁ + R₂ + R₃
I same for all
V = V₁ + V₂ + V₃
```

#### Parallel
```
┌─────────────────────────────────────────────────────────────┐
│              Resistors in Parallel                           │
└─────────────────────────────────────────────────────────────┘

    ────[R₁]────
    │
    ├───[R₂]────
    │
    └───[R₃]────
    
1/R_eq = 1/R₁ + 1/R₂ + 1/R₃
V same for all
I = I₁ + I₂ + I₃
```

### Kirchhoff's Laws
```
┌─────────────────────────────────────────────────────────────┐
│              Kirchhoff's Laws                               │
└─────────────────────────────────────────────────────────────┘

Kirchhoff's Current Law (KCL):
ΣI_in = ΣI_out
(Sum of currents at junction = 0)

Kirchhoff's Voltage Law (KVL):
ΣV = 0
(Sum of voltages in closed loop = 0)

Example:
    ┌───[R₁]───┐
    │          │
    E          [R₂]
    │          │
    └──────────┘
    
Loop: E - IR₁ - IR₂ = 0
```

---

## 4. Magnetic Field

### Magnetic Field Lines
```
┌─────────────────────────────────────────────────────────────┐
│              Magnetic Field                                 │
└─────────────────────────────────────────────────────────────┘

Bar Magnet:
    N ────► S
    ╱│╲    ╱│╲
   ╱ │ ╲  ╱ │ ╲
  ╱  │  ╲╱  │  ╲
 ╱   │   │   │   ╲
╱    │   │   │    ╲
│    •   │   •    │
╲    │   │   │    ╱
 ╲   │   │   │   ╱
  ╲  │  ╲╱  │  ╱
   ╲ │ ╱  ╲ │ ╱
    ╲│╱    ╲│╱
    
Field lines:
- Form closed loops
- Never cross
- Enter S pole, exit N pole
```

### Magnetic Force
```
┌─────────────────────────────────────────────────────────────┐
│              Force on Moving Charge                         │
└─────────────────────────────────────────────────────────────┘

    B (into page)
    │
    │  × × × ×
    │  × × × ×
    │  × × × ×
    │
    └───► v (charge moving right)
    
Force:
F = q(v × B) = qvB sin θ

Direction: Right-hand rule
F = 0 when v || B
F_max = qvB when v ⊥ B
```

### Force on Current-Carrying Wire
```
┌─────────────────────────────────────────────────────────────┐
│              Force on Wire                                  │
└─────────────────────────────────────────────────────────────┘

    B
    │
    │  × × × ×
    │  × × × ×
    │  × × × ×
    │
    └───► I (current)
    
Force:
F = I(L × B) = ILB sin θ

F = 0 when I || B
F_max = ILB when I ⊥ B
```

---

## 5. Biot-Savart Law and Ampere's Law

### Magnetic Field Due to Current
```
┌─────────────────────────────────────────────────────────────┐
│              Biot-Savart Law                                │
└─────────────────────────────────────────────────────────────┘

    •───────► I
    │
    │ r
    │
    ▼
    P (point)
    
dB = (μ₀/4π)(Idl × r̂)/r²

μ₀ = 4π × 10⁻⁷ T⋅m/A
```

### Ampere's Law
```
┌─────────────────────────────────────────────────────────────┐
│              Ampere's Law                                   │
└─────────────────────────────────────────────────────────────┘

    ┌──────────┐
    │          │  ← Closed loop
    │    I     │
    │          │
    └──────────┘
    
∮B·dl = μ₀I_enclosed

Applications:
- Straight wire: B = μ₀I/(2πr)
- Solenoid: B = μ₀nI
- Toroid: B = μ₀NI/(2πr)
```

---

## 6. Electromagnetic Induction

### Faraday's Law
```
┌─────────────────────────────────────────────────────────────┐
│              Faraday's Law                                  │
└─────────────────────────────────────────────────────────────┘

    ┌──────────┐
    │          │  ← Loop
    │    B     │  (magnetic field)
    │    ×     │
    │    ×     │
    └──────────┘
    
EMF:
ε = -dΦ/dt

Φ = BA cos θ (magnetic flux)
Lenz's Law: Induced current opposes change
```

### Lenz's Law
```
┌─────────────────────────────────────────────────────────────┐
│              Lenz's Law                                     │
└─────────────────────────────────────────────────────────────┘

Magnet moving towards loop:
    N ────► ┌────┐
            │    │  ← Loop
            └────┘
            
Induced current creates field opposing magnet
(Repels magnet)

Magnet moving away:
Induced current creates field attracting magnet
(Attracts magnet)
```

---

## 7. AC Circuits

### AC Voltage and Current
```
┌─────────────────────────────────────────────────────────────┐
│              AC Waveform                                    │
└─────────────────────────────────────────────────────────────┘

Voltage (V)
    │
    │     ╱╲       ╱╲
    │    ╱  ╲     ╱  ╲
    │   ╱    ╲   ╱    ╲
    │  ╱      ╲ ╱      ╲
    │ ╱        ╲        ╲
    │╱          ╲          ╲
    └──────────────────────────► Time
    
V = V₀ sin(ωt)
I = I₀ sin(ωt + φ)

RMS values:
V_rms = V₀/√2
I_rms = I₀/√2
```

### RLC Circuit
```
┌─────────────────────────────────────────────────────────────┐
│              RLC Series Circuit                              │
└─────────────────────────────────────────────────────────────┘

    ────[R]────[L]────[C]────
    
Impedance:
Z = √(R² + (X_L - X_C)²)

X_L = ωL (inductive reactance)
X_C = 1/(ωC) (capacitive reactance)

Phase angle:
tan φ = (X_L - X_C)/R

Resonance:
ω₀ = 1/√(LC)
At resonance: X_L = X_C, Z = R (minimum)
```

---

## Key Formulas Summary

### Electrostatics
```
E = kq/r² = q/(4πε₀r²)
V = kq/r = q/(4πε₀r)
F = qE
W = qΔV
```

### Capacitance
```
C = Q/V
C = ε₀A/d (parallel plate)
C = 4πε₀R (sphere)
U = ½CV²
```

### Current
```
I = Q/t = nAve
V = IR
R = ρL/A
P = VI = I²R = V²/R
```

### Magnetism
```
F = q(v × B) = qvB sin θ
F = I(L × B) = ILB sin θ
B = μ₀I/(2πr) (straight wire)
B = μ₀nI (solenoid)
```

### Induction
```
ε = -dΦ/dt
Φ = BA cos θ
ε = -L(dI/dt) (self-induction)
```

### AC
```
V_rms = V₀/√2
Z = √(R² + (X_L - X_C)²)
ω₀ = 1/√(LC)
```

---

**Next: Part 6 will cover Modern Physics.**

