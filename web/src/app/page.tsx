"use client";

import { motion } from "framer-motion";
import { ArrowRight, Download, Map, Mic, FileText, Code, Users, Target } from "lucide-react";

export default function Home() {
  const fadeInUp = {
    hidden: { opacity: 0, y: 40 },
    visible: { opacity: 1, y: 0, transition: { duration: 0.8, ease: [0.16, 1, 0.3, 1] } }
  };

  const scaleUp = {
    hidden: { opacity: 0, scale: 0.8 },
    visible: { opacity: 1, scale: 1, transition: { duration: 0.8, type: "spring", bounce: 0.4 } }
  };

  const staggerContainer = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: { staggerChildren: 0.15 }
    }
  };

  return (
    <main>
      {/* Background Glowing Orbs */}
      <div className="bg-glow-container">
        <div className="bg-glow glow-1"></div>
        <div className="bg-glow glow-2"></div>
        <div className="bg-glow glow-3"></div>
      </div>

      {/* Hero Section */}
      <section className="hero container">
        <div className="hero-grid">
          <motion.div 
            className="hero-content"
            initial="hidden"
            animate="visible"
            variants={staggerContainer}
          >
            <motion.h1 variants={fadeInUp}>
              Navigate Your Future with <span>AI-CareerPilot</span>
            </motion.h1>
            <motion.p variants={fadeInUp}>
              Your personal AI career mentor. Experience personalized roadmaps, realistic voice interview simulations, and intelligent resume analysis powered by Qwen 3.8-27b.
            </motion.p>
            <motion.div className="hero-actions" variants={fadeInUp}>
              <a href="https://github.com/zaidkhannn/AI-CareerPilot/releases" target="_blank" rel="noopener noreferrer" className="btn btn-primary">
                <Download size={22} /> Download App
              </a>
              <a href="#overview" className="btn btn-secondary">
                Explore Project <ArrowRight size={22} />
              </a>
              <a href="https://github.com/zaidkhannn/AI-CareerPilot" target="_blank" rel="noopener noreferrer" className="btn btn-secondary">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M15 22v-4a4.8 4.8 0 0 0-1-3.2c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36-.5-8 0C6 2 5 2 5 2c-.3 1.15-.3 2.35 0 3.5A5.403 5.403 0 0 0 4 9c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4"/><path d="M9 18c-4.51 2-5-2-7-2"/></svg> View Source
              </a>
            </motion.div>
          </motion.div>

          <motion.div 
            className="hero-visual"
            initial={{ opacity: 0, scale: 0.8, rotateY: -20, rotateX: 10 }}
            animate={{ opacity: 1, scale: 1, rotateY: 0, rotateX: 0 }}
            transition={{ duration: 1.5, type: "spring", bounce: 0.3 }}
          >
            <div className="decor-ring" style={{ width: '100%', height: '100%' }}></div>
            <div className="decor-ring decor-ring-2" style={{ width: '70%', height: '70%' }}></div>
            <div className="phone-mockup">
              <div className="phone-notch"></div>
              <div className="phone-screen">
                <img src="/screenshots/dashboard.png" alt="Dashboard" onError={(e) => {
                  e.currentTarget.style.display = 'none';
                  e.currentTarget.parentElement!.innerHTML += '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#00f2fe;text-align:center;padding:2rem;">[ Missing dashboard.png ]</div>';
                }} />
              </div>
            </div>
          </motion.div>
        </div>
      </section>

      {/* Project Overview */}
      <section id="overview" className="section container">
        <motion.div 
          initial="hidden" whileInView="visible" viewport={{ once: true, margin: "-100px" }} variants={fadeInUp}
        >
          <h2 className="section-title gradient-text">Project Overview</h2>
          <p className="section-subtitle">A native Android experience built for the modern job seeker.</p>
          
          <div className="glass-panel" style={{ padding: '4rem 3rem', textAlign: 'center', maxWidth: '900px', margin: '0 auto', fontSize: '1.35rem', lineHeight: '1.9' }}>
            <p style={{ marginBottom: '2rem' }}>
              <strong style={{ color: 'var(--primary-color)' }}>AI-CareerPilot</strong> is an innovative Android application bridging the gap between talent and opportunity. Built for students and professionals, it provides structured guidance and realistic interview practice.
            </p>
            <p style={{ color: 'var(--text-secondary)' }}>
              Leveraging the powerful Groq API, the app acts as an elite personal mentor. It delivers dynamic career mapping and immersive voice-to-voice interview simulations directly on your device.
            </p>
          </div>
        </motion.div>
      </section>

      {/* Actual Application Screenshots */}
      <section id="screenshots" className="section container">
        <motion.div initial="hidden" whileInView="visible" viewport={{ once: true, margin: "-100px" }} variants={fadeInUp}>
          <h2 className="section-title gradient-text">The Application</h2>
          <p className="section-subtitle">Real screenshots from the Android implementation.</p>
          
          <div className="screenshots-gallery">
            {[
              { id: 1, name: "Career Roadmap", file: "roadmap.png" },
              { id: 2, name: "Interview Simulator", file: "interview.png" },
              { id: 3, name: "AI Evaluation", file: "evaluation.png" }
            ].map((shot, index) => (
              <motion.div 
                className="screenshot-item" 
                key={shot.id}
                initial={{ opacity: 0, y: 60, scale: 0.9 }}
                whileInView={{ opacity: 1, y: 0, scale: 1 }}
                viewport={{ once: true }}
                transition={{ delay: index * 0.15, duration: 0.8, type: "spring" }}
              >
                <img src={`/screenshots/${shot.file}`} alt={shot.name} onError={(e) => {
                  e.currentTarget.style.display = 'none';
                }} />
              </motion.div>
            ))}
          </div>
        </motion.div>
      </section>

      {/* How it Works Workflow */}
      <section id="workflow" className="section container">
        <h2 className="section-title gradient-text">Application Workflow</h2>
        <p className="section-subtitle">A seamless journey from onboarding to AI guidance.</p>
        
        <div className="workflow-container">
          <div className="workflow-line"></div>
          
          {[
            { step: "1", title: "Onboarding & Profile", desc: "User logs in and sets up their initial career profile and goals." },
            { step: "2", title: "Main Dashboard", desc: "Access core AI features via a modern, glassmorphic UI." },
            { step: "3", title: "Input & Assessment", desc: "Provide target role details or use Voice Input in the simulator." },
            { step: "4", title: "AI Processing", desc: "Dynamic prompts are processed via Groq API (Qwen 3.8-27b) seamlessly in the background." },
            { step: "5", title: "Guidance & Results", desc: "Rich Markdown output rendered with Markwon, and realistic Voice output via Android TTS." }
          ].map((item, index) => (
            <motion.div 
              className="workflow-step" 
              key={index}
              initial={{ opacity: 0, x: -50 }}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true, margin: "-100px" }}
              transition={{ delay: index * 0.15, duration: 0.7, type: "spring" }}
            >
              <div className="workflow-number">{item.step}</div>
              <div className="workflow-content glass-panel">
                <h3>{item.title}</h3>
                <p>{item.desc}</p>
              </div>
            </motion.div>
          ))}
        </div>
      </section>

      {/* Key Features */}
      <section id="features" className="section container">
        <h2 className="section-title gradient-text">Core Features</h2>
        <p className="section-subtitle">Everything you need to land your dream job.</p>
        
        <motion.div 
          className="features-grid"
          variants={staggerContainer}
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, margin: "-100px" }}
        >
          {[
            { icon: <Map size={32} />, title: "Career Roadmap", desc: "Detailed, phase-by-phase Markdown learning roadmaps based on target role and daily commitment." },
            { icon: <Mic size={32} />, title: "Interview Simulator", desc: "Immersive voice-to-voice interviews using Android's native STT and TTS, driven by an adaptive AI." },
            { icon: <FileText size={32} />, title: "Resume Analyzer", desc: "Reviews resumes against ATS standards and provides actionable, targeted feedback." },
            { icon: <Code size={32} />, title: "Technical Assessment", desc: "Evaluates technical skills with targeted questions tailored to your desired path." },
            { icon: <Users size={32} />, title: "Mentor Connect", desc: "Offers expert guidance on career progression and invaluable industry insights." },
            { icon: <Target size={32} />, title: "Dream Job Planner", desc: "Helps structure and plan out long-term career goals efficiently and realistically." }
          ].map((feature, index) => (
            <motion.div className="feature-card glass-panel" key={index} variants={scaleUp}>
              <div className="feature-icon-wrapper">
                {feature.icon}
              </div>
              <h3>{feature.title}</h3>
              <p>{feature.desc}</p>
            </motion.div>
          ))}
        </motion.div>
      </section>

      {/* Technology Stack */}
      <section id="tech" className="section container">
        <h2 className="section-title gradient-text">Technology Stack</h2>
        <p className="section-subtitle">Built with robust, modern native tools.</p>
        
        <motion.div 
          className="tech-grid"
          variants={staggerContainer}
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, margin: "-100px" }}
        >
          {["🤖 Android SDK", "☕ Java", "🎨 XML & Material Design", "🧠 Groq API", "🗣️ Android TTS & STT", "📝 Markwon", "🌐 OkHttp3", "💾 SharedPreferences"].map((tech, i) => (
             <motion.div 
               className="tech-badge" 
               key={i}
               variants={scaleUp}
             >
               {tech}
             </motion.div>
          ))}
        </motion.div>
      </section>

      {/* CTA Footer Section */}
      <section className="section container" style={{ textAlign: 'center', marginBottom: '6rem' }}>
        <motion.div
           initial={{ opacity: 0, y: 50, scale: 0.95 }}
           whileInView={{ opacity: 1, y: 0, scale: 1 }}
           viewport={{ once: true }}
           transition={{ duration: 0.8, type: "spring" }}
           className="glass-panel"
           style={{ padding: '6rem 3rem', background: 'var(--surface-color)', borderColor: 'var(--primary-color)' }}
        >
          <h2 className="section-title gradient-text" style={{ marginBottom: '2rem' }}>Ready to Explore?</h2>
          <div className="hero-actions" style={{ justifyContent: 'center' }}>
             <a href="https://github.com/zaidkhannn/AI-CareerPilot/releases" target="_blank" rel="noopener noreferrer" className="btn btn-primary" style={{ padding: '1.2rem 2.5rem', fontSize: '1.25rem' }}>
               <Download size={24} /> Download App
             </a>
             <a href="https://github.com/zaidkhannn/AI-CareerPilot" target="_blank" rel="noopener noreferrer" className="btn btn-secondary" style={{ padding: '1.2rem 2.5rem', fontSize: '1.25rem', background: 'rgba(255,255,255,0.05)', color: '#fff', border: '1px solid rgba(255,255,255,0.2)' }}>
               <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M15 22v-4a4.8 4.8 0 0 0-1-3.2c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36-.5-8 0C6 2 5 2 5 2c-.3 1.15-.3 2.35 0 3.5A5.403 5.403 0 0 0 4 9c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4"/><path d="M9 18c-4.51 2-5-2-7-2"/></svg> View Source on GitHub
             </a>
          </div>
        </motion.div>
      </section>

      <footer>
        <div className="container">
          <p>© {new Date().getFullYear()} AI-CareerPilot Showcase. Created by Zaid Khan.</p>
        </div>
      </footer>
    </main>
  );
}
