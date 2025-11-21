/* ========================================
   PIXEL SHOP - ANIMACIÓN DE PARTÍCULAS
   Para lado izquierdo del split screen
   ======================================== */

class Particle {
    constructor(canvas) {
        this.canvas = canvas;
        this.reset();
        
        // Posición inicial aleatoria
        this.x = Math.random() * canvas.width;
        this.y = Math.random() * canvas.height;
    }
    
    reset() {
        // Velocidad aleatoria
        const speed = 0.5;
        this.vx = (Math.random() - 0.5) * speed;
        this.vy = (Math.random() - 0.5) * speed;
        
        // Propiedades visuales
        this.radius = Math.random() * 3 + 1;
        this.hue = Math.random() * 60 + 240; // Azul-morado (240-300)
        this.alpha = Math.random() * 0.5 + 0.5;
        this.life = 1;
    }
    
    update() {
        // Mover partícula
        this.x += this.vx;
        this.y += this.vy;
        
        // Rebotar en los bordes con efecto suave
        if (this.x < 0 || this.x > this.canvas.width) {
            this.vx *= -1;
            this.x = Math.max(0, Math.min(this.canvas.width, this.x));
        }
        if (this.y < 0 || this.y > this.canvas.height) {
            this.vy *= -1;
            this.y = Math.max(0, Math.min(this.canvas.height, this.y));
        }
    }
    
    draw(ctx) {
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
        
        // Gradiente radial para cada partícula
        const gradient = ctx.createRadialGradient(
            this.x, this.y, 0,
            this.x, this.y, this.radius
        );
        gradient.addColorStop(0, `hsla(${this.hue}, 80%, 70%, ${this.alpha})`);
        gradient.addColorStop(1, `hsla(${this.hue}, 80%, 50%, 0)`);
        
        ctx.fillStyle = gradient;
        ctx.fill();
    }
}

class ParticleSystem {
    constructor(canvasId) {
        this.canvas = document.getElementById(canvasId);
        if (!this.canvas) {
            console.error('Canvas no encontrado');
            return;
        }
        
        this.ctx = this.canvas.getContext('2d');
        this.particles = [];
        this.particleCount = 100;
        this.maxDistance = 150;
        this.mouse = { x: 0, y: 0 };
        this.mouseRadius = 100;
        
        this.resize();
        this.init();
        this.addMouseListener();
        this.animate();
        
        // Redimensionar cuando cambie el tamaño de ventana
        window.addEventListener('resize', () => this.resize());
    }
    
    resize() {
        const leftSide = this.canvas.parentElement;
        if (leftSide) {
            this.canvas.width = leftSide.clientWidth;
            this.canvas.height = leftSide.clientHeight;
        } else {
            this.canvas.width = window.innerWidth / 2;
            this.canvas.height = window.innerHeight;
        }
    }
    
    init() {
        this.particles = [];
        for (let i = 0; i < this.particleCount; i++) {
            this.particles.push(new Particle(this.canvas));
        }
    }
    
    addMouseListener() {
        this.canvas.addEventListener('mousemove', (e) => {
            const rect = this.canvas.getBoundingClientRect();
            this.mouse.x = e.clientX - rect.left;
            this.mouse.y = e.clientY - rect.top;
        });
        
        this.canvas.addEventListener('mouseleave', () => {
            this.mouse.x = -1000;
            this.mouse.y = -1000;
        });
    }
    
    connectParticles() {
        for (let i = 0; i < this.particles.length; i++) {
            for (let j = i + 1; j < this.particles.length; j++) {
                const dx = this.particles[i].x - this.particles[j].x;
                const dy = this.particles[i].y - this.particles[j].y;
                const distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance < this.maxDistance) {
                    const opacity = (1 - distance / this.maxDistance) * 0.4;
                    
                    // Gradiente en la línea
                    const gradient = this.ctx.createLinearGradient(
                        this.particles[i].x, this.particles[i].y,
                        this.particles[j].x, this.particles[j].y
                    );
                    
                    gradient.addColorStop(0, `hsla(${this.particles[i].hue}, 80%, 70%, ${opacity})`);
                    gradient.addColorStop(1, `hsla(${this.particles[j].hue}, 80%, 70%, ${opacity})`);
                    
                    this.ctx.beginPath();
                    this.ctx.strokeStyle = gradient;
                    this.ctx.lineWidth = 1.5;
                    this.ctx.moveTo(this.particles[i].x, this.particles[i].y);
                    this.ctx.lineTo(this.particles[j].x, this.particles[j].y);
                    this.ctx.stroke();
                }
            }
        }
    }
    
    interactWithMouse() {
        for (let particle of this.particles) {
            const dx = particle.x - this.mouse.x;
            const dy = particle.y - this.mouse.y;
            const distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance < this.mouseRadius) {
                const force = (this.mouseRadius - distance) / this.mouseRadius;
                const angle = Math.atan2(dy, dx);
                particle.vx += Math.cos(angle) * force * 0.2;
                particle.vy += Math.sin(angle) * force * 0.2;
            }
            
            // Limitar velocidad máxima
            const speed = Math.sqrt(particle.vx ** 2 + particle.vy ** 2);
            if (speed > 2) {
                particle.vx = (particle.vx / speed) * 2;
                particle.vy = (particle.vy / speed) * 2;
            }
        }
    }
    
    animate() {
        // Efecto de desvanecimiento suave
        this.ctx.fillStyle = 'rgba(30, 60, 114, 0.08)';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
        
        // Interacción con mouse
        this.interactWithMouse();
        
        // Actualizar y dibujar partículas
        for (let particle of this.particles) {
            particle.update();
            particle.draw(this.ctx);
        }
        
        // Conectar partículas cercanas
        this.connectParticles();
        
        // Continuar animación
        requestAnimationFrame(() => this.animate());
    }
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('particleCanvas');
    if (canvas) {
        new ParticleSystem('particleCanvas');
    }
});

