import Navigation from './components/layout/Navigation';
import Hero from './components/sections/Hero';
import CaseSearchSection from './components/sections/CaseSearchSection';
import Features from './components/sections/Features';
import HowItWorks from './components/sections/HowItWorks';
import Benefits from './components/sections/Benefits';
import CTA from './components/sections/CTA';
import Footer from './components/layout/Footer';

export default function Home() {
  return (
    <div className="min-h-screen bg-white">
      <Navigation />
      <Hero />
      <CaseSearchSection />
      <Features />
      <HowItWorks />
      <Benefits />
      <CTA />
      <Footer />
    </div>
  );
}
