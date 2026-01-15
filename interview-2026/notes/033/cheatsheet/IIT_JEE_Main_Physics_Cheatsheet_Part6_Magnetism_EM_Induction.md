# IIT JEE Main Physics Cheatsheet - Part 6: Magnetism & Electromagnetic Induction

## 🧲 Magnetism

### Magnetic Field

**Biot-Savart Law:**
```
dB = (μ₀/4π) × (Idl × r̂)/r²
dB = (μ₀/4π) × (Idl sin θ)/r²
```

**Where:**
- `μ₀ = 4π × 10⁻⁷ T·m/A` (permeability of free space)

### Magnetic Field Due to Current

**Straight wire:**
```
B = μ₀I/(2πr)
Direction: Right-hand rule
```

**Circular loop (center):**
```
B = μ₀I/(2R) = μ₀NI/(2R) (for N turns)
```

**Circular loop (on axis):**
```
B = (μ₀IR²)/[2(R² + x²)^(3/2)]
At center (x = 0): B = μ₀I/(2R)
```

**Solenoid:**
```
B = μ₀nI (inside, far from ends)
where n = turns per unit length
```

**Toroid:**
```
B = μ₀NI/(2πr)
where r = distance from center
```

### Ampere's Law

**Statement:**
```
∮ B⃗ · dl⃗ = μ₀I_enclosed
```

**Applications:**
- Straight wire: `B = μ₀I/(2πr)`
- Solenoid: `B = μ₀nI`
- Toroid: `B = μ₀NI/(2πr)`

### Force on Current-Carrying Conductor

**Force:**
```
F⃗ = I(l⃗ × B⃗)
F = IlB sin θ
```

**Force on straight wire:**
```
F = BIl sin θ
```

**Force between parallel wires:**
```
F/l = μ₀I₁I₂/(2πd)
Attractive if currents in same direction
Repulsive if opposite
```

### Force on Moving Charge

**Lorentz force:**
```
F⃗ = q(v⃗ × B⃗)
F = qvB sin θ
```

**Motion in magnetic field:**
- Perpendicular to field: Circular motion
- Parallel to field: Straight line
- At angle: Helical motion

**Radius of circular path:**
```
r = mv/(qB) = p/(qB)
```

**Time period:**
```
T = 2πm/(qB)
```

**Frequency (cyclotron):**
```
f = qB/(2πm)
```

**Pitch of helix:**
```
p = v_parallel × T = (2πmv cos θ)/(qB)
```

### Magnetic Moment

**Definition:**
```
μ⃗ = IA⃗ = IAn̂
```

**Torque:**
```
τ⃗ = μ⃗ × B⃗
τ = μB sin θ = IAB sin θ
```

**Potential energy:**
```
U = -μ⃗ · B⃗ = -μB cos θ
```

### Magnetic Materials

**Magnetic field in material:**
```
B = μ₀(H + M) = μH
where H = magnetic intensity
M = magnetization
μ = permeability
```

**Relative permeability:**
```
μ_r = μ/μ₀ = 1 + χ
where χ = magnetic susceptibility
```

**Types:**
- Diamagnetic: `χ < 0, μ_r < 1`
- Paramagnetic: `χ > 0, μ_r > 1`
- Ferromagnetic: `χ >> 1, μ_r >> 1`

---

## ⚡ Electromagnetic Induction

### Faraday's Law

**EMF induced:**
```
ε = -dΦ/dt
```

**Where:**
- `Φ = B⃗ · A⃗ = BA cos θ` (magnetic flux)

**Lenz's Law:**
- Induced current opposes the change causing it
- Determines direction of induced EMF

### Motional EMF

**Conductor moving in field:**
```
ε = Blv
where l = length, v = velocity
```

**Direction:**
- Right-hand rule or Fleming's right-hand rule

**Power:**
```
P = Fv = (B²l²v²)/R
```

### Self-Induction

**Self-inductance:**
```
L = Φ/I = NΦ/I
```

**EMF:**
```
ε = -L(dI/dt)
```

**Energy stored:**
```
U = ½LI²
```

**Inductor:**
- Opposes change in current
- Acts as open circuit for DC (steady state)
- Acts as short circuit for high frequency AC

### Mutual Induction

**Mutual inductance:**
```
M = Φ₂₁/I₁ = Φ₁₂/I₂
```

**EMF:**
```
ε₂ = -M(dI₁/dt)
ε₁ = -M(dI₂/dt)
```

**Coefficient of coupling:**
```
k = M/√(L₁L₂)
0 ≤ k ≤ 1
```

### LR Circuit

**Growth of current:**
```
I = (ε/R)(1 - e^(-t/τ))
where τ = L/R (time constant)
```

**Decay of current:**
```
I = I₀e^(-t/τ)
```

**Time constant:**
```
τ = L/R
```

### LC Oscillations

**Charge:**
```
q = Q₀ cos(ωt + φ)
```

**Current:**
```
I = -Q₀ω sin(ωt + φ)
```

**Angular frequency:**
```
ω = 1/√(LC)
```

**Frequency:**
```
f = 1/(2π√(LC))
```

**Energy:**
```
U_total = ½LI² + q²/(2C) = constant
U_E = q²/(2C)
U_B = ½LI²
```

---

## 📊 Quick Reference

### Constants
- `μ₀ = 4π × 10⁻⁷ T·m/A`
- `ε₀ = 8.85 × 10⁻¹² C²/N·m²`
- `c = 1/√(μ₀ε₀) = 3 × 10⁸ m/s`

### Units
- Magnetic field: T (Tesla) = N/(A·m) = Wb/m²
- Magnetic flux: Wb (Weber) = T·m² = V·s
- Inductance: H (Henry) = Wb/A = V·s/A

### Important Relations
- `F = q(v × B)`
- `F = I(l × B)`
- `τ = μ × B`
- `r = mv/(qB)`
- `ε = -dΦ/dt`
- `ε = Blv`
- `U = ½LI²`

### Right-Hand Rules
1. **Current → Field**: Curl fingers in direction of current, thumb gives field direction
2. **Force on current**: Point fingers in direction of current, curl toward B, thumb gives force
3. **Force on charge**: Point fingers in direction of v, curl toward B, thumb gives force (for +ve charge)

### Common Mistakes
1. Confusing magnetic field direction
2. Wrong sign in Faraday's law
3. Forgetting Lenz's law direction
4. Confusing self and mutual inductance
5. Not considering back EMF in motors

---

**Next: Part 7 - Optics (Geometric & Wave)**

