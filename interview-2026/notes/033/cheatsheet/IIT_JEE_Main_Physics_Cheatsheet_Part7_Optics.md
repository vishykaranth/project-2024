# IIT JEE Main Physics Cheatsheet - Part 7: Optics (Geometric & Wave)

## 🔍 Geometric Optics

### Reflection

**Laws of Reflection:**
1. Angle of incidence = Angle of reflection
2. Incident ray, normal, and reflected ray lie in same plane

**Plane mirror:**
```
Image distance = Object distance
Magnification: m = +1 (virtual, erect, same size)
Lateral inversion occurs
```

**Spherical mirrors:**

**Mirror equation:**
```
1/f = 1/v + 1/u
```

**Magnification:**
```
m = -v/u = h'/h
```

**Focal length:**
```
f = R/2
```

**Sign convention:**
- Object distance (u): -ve (real), +ve (virtual)
- Image distance (v): -ve (real), +ve (virtual)
- Focal length (f): -ve (concave), +ve (convex)
- Height: +ve (above axis), -ve (below axis)

**For concave mirror:**
- Real image: v = -ve, m = -ve
- Virtual image: v = +ve, m = +ve

**For convex mirror:**
- Always virtual: v = +ve, m = +ve

### Refraction

**Snell's Law:**
```
n₁ sin i = n₂ sin r
μ₁ sin i = μ₂ sin r
```

**Refractive index:**
```
n = c/v = sin i/sin r
```

**Critical angle:**
```
sin C = n₂/n₁ = 1/n (for air)
C = sin⁻¹(1/n)
```

**Total internal reflection:**
- Occurs when: `i > C` and light travels from denser to rarer medium

### Spherical Lenses

**Lens equation:**
```
1/f = 1/v - 1/u
```

**Lens maker's formula:**
```
1/f = (n - 1)(1/R₁ - 1/R₂)
```

**Power:**
```
P = 1/f (in diopters, f in meters)
```

**Magnification:**
```
m = v/u = h'/h
```

**Sign convention:**
- Object distance (u): -ve
- Image distance (v): +ve (real), -ve (virtual)
- Focal length: +ve (convex), -ve (concave)
- Radius: +ve (convex), -ve (concave)

**For convex lens:**
- Real image: v = +ve, m = -ve (inverted)
- Virtual image: v = -ve, m = +ve (erect)

**For concave lens:**
- Always virtual: v = -ve, m = +ve

### Combination of Lenses

**In contact:**
```
1/f_eq = 1/f₁ + 1/f₂
P_eq = P₁ + P₂
```

**Separated by distance d:**
```
1/f_eq = 1/f₁ + 1/f₂ - d/(f₁f₂)
```

### Prism

**Angle of deviation:**
```
δ = i + e - A
where A = angle of prism
```

**Minimum deviation:**
```
δ_min = 2i - A (when i = e)
n = sin[(A + δ_min)/2] / sin(A/2)
```

**Dispersion:**
```
Angular dispersion: δ_v - δ_r = A(n_v - n_r)
Dispersive power: ω = (n_v - n_r)/(n_y - 1)
where n_y = mean refractive index
```

### Optical Instruments

**Simple microscope:**
```
m = D/f (normal adjustment)
m = 1 + D/f (when image at infinity)
```

**Compound microscope:**
```
m = m₀ × m_e = (v₀/u₀) × (D/f_e)
```

**Astronomical telescope:**
```
m = -f₀/f_e (normal adjustment)
Length: L = f₀ + f_e
```

**Terrestrial telescope:**
```
m = -f₀/f_e
Length: L = f₀ + 4f + f_e (erecting lens)
```

---

## 🌈 Wave Optics

### Interference

**Young's double slit:**
```
Path difference: Δx = d sin θ ≈ dθ ≈ dy/D
```

**Fringe width:**
```
β = λD/d
```

**Position of bright fringes:**
```
y_n = nλD/d, n = 0, 1, 2, ...
```

**Position of dark fringes:**
```
y_n = (n + ½)λD/d, n = 0, 1, 2, ...
```

**Intensity:**
```
I = 4I₀ cos²(πd sin θ/λ)
I_max = 4I₀
I_min = 0
```

**Phase difference:**
```
Δφ = (2π/λ) × path difference
```

### Diffraction

**Single slit:**
```
Angular width of central maximum: 2θ = 2λ/a
Linear width: w = 2λD/a
```

**Position of minima:**
```
a sin θ = nλ, n = ±1, ±2, ...
```

**Position of secondary maxima:**
```
a sin θ ≈ (n + ½)λ, n = ±1, ±2, ...
```

**Intensity:**
```
I = I₀[sin(πa sin θ/λ)/(πa sin θ/λ)]²
```

**Diffraction grating:**
```
d sin θ = nλ (principal maxima)
where d = grating element = 1/N (N = lines per unit length)
```

**Resolving power:**
```
R = λ/Δλ = nN
```

### Polarization

**Malus' Law:**
```
I = I₀ cos²θ
where θ = angle between polarizer and analyzer
```

**Brewster's Law:**
```
tan i_p = n
i_p + r = 90°
```

**Polarizing angle:**
```
i_p = tan⁻¹(n)
```

**Polarization by reflection:**
- When `i = i_p`, reflected light is completely polarized

**Polarization by scattering:**
- Sky appears blue due to scattering
- Scattered light is polarized

### Huygens' Principle

- Every point on wavefront acts as source of secondary wavelets
- New wavefront is envelope of secondary wavelets

---

## 📊 Quick Reference

### Constants
- Speed of light: `c = 3 × 10⁸ m/s`
- Refractive index of air: `n ≈ 1`
- Refractive index of water: `n ≈ 1.33`
- Refractive index of glass: `n ≈ 1.5`

### Units
- Focal length: m
- Power: D (Diopter) = m⁻¹
- Wavelength: m, nm, Å

### Important Relations
- `1/f = 1/v + 1/u` (mirror)
- `1/f = 1/v - 1/u` (lens)
- `m = -v/u`
- `n = c/v = sin i/sin r`
- `β = λD/d` (fringe width)
- `I = I₀ cos²θ` (Malus' law)

### Sign Conventions

**Mirrors:**
- u: -ve (real object)
- v: -ve (real image), +ve (virtual)
- f: -ve (concave), +ve (convex)

**Lenses:**
- u: -ve (real object)
- v: +ve (real image), -ve (virtual)
- f: +ve (convex), -ve (concave)

### Common Mistakes
1. Wrong sign convention
2. Confusing real and virtual images
3. Forgetting magnification sign
4. Confusing interference and diffraction
5. Wrong formula for lens vs mirror

---

**Next: Part 8 - Modern Physics**

