import type { Metadata, Viewport } from "next";
import "./globals.css";

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
};

export const metadata: Metadata = {
  title: "AI-CareerPilot | AI-Powered Career Guidance",
  description: "An AI-powered Android application designed to help users explore and navigate career opportunities through personalized roadmaps, interview simulations, and resume analysis.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        <nav className="navbar">
          <div className="container navbar-content">
            <div className="nav-brand">AI-CareerPilot</div>
            <div className="nav-links">
              <a href="#overview" className="nav-link">Overview</a>
              <a href="#features" className="nav-link">Features</a>
              <a href="#workflow" className="nav-link">How it Works</a>
              <a href="#tech" className="nav-link">Technology</a>
              <a href="https://github.com/zaidkhannn/AI-CareerPilot" target="_blank" rel="noopener noreferrer" className="nav-link">GitHub</a>
            </div>
          </div>
        </nav>
        {children}
      </body>
    </html>
  );
}
