import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "yawr — downloader tester",
  description: "Lightweight web tester for yawr downloads.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="en"><body>{children}</body></html>;
}
