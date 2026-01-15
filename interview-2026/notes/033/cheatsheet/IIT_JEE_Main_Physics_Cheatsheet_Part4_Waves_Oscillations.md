# IIT JEE Main Physics Cheatsheet - Part 4: Waves & Oscillations

## 🔄 Simple Harmonic Motion (SHM)

### Basic Equations

**Displacement:**
```
x = A sin(ωt + φ)
x = A cos(ωt + φ)
```

**Velocity:**
```
v = Aω cos(ωt + φ) = ω√(A² - x²)
v_max = Aω
```

**Acceleration:**
```
a = -Aω² sin(ωt + φ) = -ω²x
a_max = Aω²
```

**Force:**
```
F = -kx = -mω²x
```

**Angular frequency:**
```
ω = √(k/m) = 2πf = 2π/T
```

**Time period:**
```
T = 2π√(m/k) = 2π/ω
```

**Frequency:**
```
f = 1/T = ω/2π
```

### Energy in SHM

**Potential Energy:**
```
U = ½kx² = ½mω²x² = ½mω²A² sin²(ωt + φ)
U_max = ½kA² = ½mω²A²
```

**Kinetic Energy:**
```
K = ½mv² = ½mω²(A² - x²) = ½mω²A² cos²(ωt + φ)
K_max = ½mω²A²
```

**Total Energy:**
```
E = U + K = ½kA² = ½mω²A² = constant
```

**Energy distribution:**
- At mean position: `K = E, U = 0`
- At extreme position: `K = 0, U = E`
- At x = A/2: `K = 3E/4, U = E/4`

### Phase and Phase Difference

**Phase:**
```
φ = ωt + φ₀
```

**Phase difference:**
```
Δφ = φ₂ - φ₁ = ω(t₂ - t₁)
```

**For same frequency:**
- In phase: `Δφ = 0, 2π, 4π, ...`
- Out of phase: `Δφ = π, 3π, 5π, ...`
- Quadrature: `Δφ = π/2, 3π/2, ...`

### Common SHM Systems

**Spring-mass system:**
```
T = 2π√(m/k)
```

**Simple pendulum:**
```
T = 2π√(L/g)
f = (1/2π)√(g/L)
```

**Physical pendulum:**
```
T = 2π√(I/mgd)
where d = distance from pivot to CM
```

**Torsional pendulum:**
```
T = 2π√(I/κ)
where κ = torsional constant
```

**Liquid in U-tube:**
```
T = 2π√(h/g)
where h = height of liquid column
```

**Spring in series:**
```
1/k_eq = 1/k₁ + 1/k₂
T = 2π√(m/k_eq)
```

**Spring in parallel:**
```
k_eq = k₁ + k₂
T = 2π√(m/k_eq)
```

### Damped Oscillations

**Displacement:**
```
x = Ae^(-bt/2m) cos(ω't + φ)
```

**Angular frequency:**
```
ω' = √(ω₀² - (b/2m)²)
where b = damping constant
```

**Time period:**
```
T' = 2π/ω'
```

**Amplitude:**
```
A(t) = A₀e^(-bt/2m)
```

### Forced Oscillations & Resonance

**Resonance frequency:**
```
ω_r = ω₀ = √(k/m)
```

**Amplitude at resonance:**
```
A_max = F₀/(bω₀)
```

**Quality factor:**
```
Q = ω₀/Δω = ω₀/(b/m)
```

---

## 🌊 Wave Motion

### Wave Equation

**General form:**
```
y(x,t) = A sin(kx - ωt + φ)
y(x,t) = A sin(2π(x/λ - t/T) + φ)
```

**Wave number:**
```
k = 2π/λ
```

**Angular frequency:**
```
ω = 2πf = 2π/T
```

**Wave speed:**
```
v = λf = ω/k = λ/T
```

**For string:**
```
v = √(T/μ)
where T = tension, μ = linear density
```

**For sound in gas:**
```
v = √(γP/ρ) = √(γRT/M)
```

### Wave Properties

**Wavelength:**
```
λ = v/f = vT
```

**Frequency:**
```
f = v/λ = 1/T
```

**Phase velocity:**
```
v_p = ω/k = λf
```

**Group velocity:**
```
v_g = dω/dk
```

### Power and Intensity

**Power:**
```
P = ½μω²A²v
```

**Intensity:**
```
I = P/A = ½ρvω²A²
I ∝ A²
I ∝ f²
```

**For spherical wave:**
```
I ∝ 1/r²
A ∝ 1/r
```

### Superposition Principle

**Resultant displacement:**
```
y = y₁ + y₂
```

**For two waves:**
```
y = 2A cos(Δφ/2) sin(kx - ωt + φ_avg)
```

**Amplitude:**
```
A_resultant = 2A|cos(Δφ/2)|
```

### Interference

**Constructive interference:**
```
Δφ = 2nπ, n = 0, 1, 2, ...
Path difference = nλ
A_max = 2A
```

**Destructive interference:**
```
Δφ = (2n+1)π, n = 0, 1, 2, ...
Path difference = (n + ½)λ
A_min = 0
```

**Intensity:**
```
I = I₁ + I₂ + 2√(I₁I₂) cos(Δφ)
I_max = (√I₁ + √I₂)²
I_min = (√I₁ - √I₂)²
```

### Standing Waves

**Wave equation:**
```
y = 2A sin(kx) cos(ωt)
```

**Nodes (displacement = 0):**
```
x = nλ/2, n = 0, 1, 2, ...
```

**Antinodes (maximum displacement):**
```
x = (n + ½)λ/2, n = 0, 1, 2, ...
```

**For string fixed at both ends:**
```
λ_n = 2L/n
f_n = nv/2L = nf₁
where f₁ = fundamental frequency
```

**For open pipe:**
```
λ_n = 2L/n
f_n = nv/2L
```

**For closed pipe:**
```
λ_n = 4L/(2n+1)
f_n = (2n+1)v/4L
```

### Beats

**Beat frequency:**
```
f_beat = |f₁ - f₂|
```

**Beat period:**
```
T_beat = 1/f_beat
```

**Resultant:**
```
y = 2A cos(2π(f₁-f₂)t/2) sin(2π(f₁+f₂)t/2)
```

### Doppler Effect

**Observer moving, source stationary:**
```
f' = f(v ± v₀)/v
+ve if observer moves toward source
-ve if observer moves away
```

**Source moving, observer stationary:**
```
f' = fv/(v ∓ v_s)
-ve if source moves toward observer
+ve if source moves away
```

**Both moving:**
```
f' = f(v ± v₀)/(v ∓ v_s)
```

**Sign convention:**
- Numerator: +ve if observer moves toward source
- Denominator: -ve if source moves toward observer

**For reflection:**
```
f' = f(v + v_s)/(v - v_s)
```

---

## 🔊 Sound Waves

### Sound Properties

**Speed in different media:**
```
v_solid > v_liquid > v_gas
```

**Speed in air:**
```
v = 331 + 0.6T m/s (T in °C)
v ≈ 330 m/s at 0°C
```

**Intensity level (decibel):**
```
β = 10 log₁₀(I/I₀)
where I₀ = 10⁻¹² W/m² (threshold of hearing)
```

**Loudness:**
```
L ∝ log I
```

### Organ Pipes

**Open pipe (both ends open):**
```
Fundamental: f₁ = v/2L
Harmonics: f_n = nf₁ = nv/2L
All harmonics present
```

**Closed pipe (one end closed):**
```
Fundamental: f₁ = v/4L
Harmonics: f_n = (2n+1)f₁ = (2n+1)v/4L
Only odd harmonics present
```

### Resonance

**Resonance length:**
```
L = (2n+1)λ/4 for closed pipe
L = nλ/2 for open pipe
```

---

## 📊 Quick Reference

### Units
- Frequency: Hz (Hertz) = s⁻¹
- Angular frequency: rad/s
- Wave number: rad/m
- Wavelength: m
- Wave speed: m/s

### Important Values
- Speed of sound in air: ~330 m/s
- Speed of light: 3 × 10⁸ m/s

### Key Relations
- `v = λf = ω/k`
- `ω = 2πf = 2π/T`
- `k = 2π/λ`
- `I ∝ A²`
- `f' = f(v ± v₀)/(v ∓ v_s)` (Doppler)

### Common Mistakes
1. Confusing wave speed with particle velocity
2. Wrong sign in Doppler effect formula
3. Forgetting that closed pipe has only odd harmonics
4. Confusing phase difference with path difference
5. Not considering both amplitude and phase in interference

---

**Next: Part 5 - Electrostatics & Current Electricity**

