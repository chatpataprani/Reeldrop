"use client";

import { FormEvent, useState } from "react";

type Result = { status: string; url?: string; filename?: string; error?: string };

export default function Home() {
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [stage, setStage] = useState("idle");
  const [result, setResult] = useState<Result | null>(null);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setResult(null);
    setStage("received");
    setLoading(true);

    try {
      setStage("resolving");
      const res = await fetch("/api/download", {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({ url: input.trim() }),
      });
      const data = await res.json();
      setResult(data);
      setStage(data.status === "success" ? "completed" : "failed");
    } catch (err) {
      setStage("failed");
      setResult({ status: "error", error: err instanceof Error ? err.message : "Request failed" });
    } finally {
      setLoading(false);
    }
  }

  const steps = [
    ["received", "✓ URL received"],
    ["resolving", "↻ Resolving media"],
    ["completed", "✓ Download ready"],
  ];

  return (
    <main>
      <section className="card">
        <h1 className="brand">yawr.</h1>
        <p className="sub">quick downloader test — no 60 MB APK required.</p>

        <form className="box" onSubmit={submit}>
          <div className="row">
            <input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="paste an Instagram Reel URL..."
              type="url"
              required
            />
            <button disabled={loading}>{loading ? "checking…" : "test download"}</button>
          </div>

          {(stage !== "idle" || result) && (
            <div className="status">
              <strong>{stage === "failed" ? "something broke 💀" : stage === "completed" ? "it worked." : "testing…"}</strong>
              <div className="steps">
                {steps.map(([key, label]) => {
                  const active = key === stage;
                  const done =
                    stage === "completed" && (key === "received" || key === "resolving");
                  return <div key={key} className={done ? "step done" : active ? "step active" : "step"}>{label}</div>;
                })}
              </div>

              {result?.error && <div className="error">{result.error}</div>}

              {result?.url && (
                <div className="result">
                  <a className="download" href={result.url} download>
                    download {result.filename || "file"}
                  </a>
                </div>
              )}
            </div>
          )}
        </form>

        <p className="hint">
          This page tests the web downloader separately from the Android app, so a failure here can be compared with the APK&apos;s error logs.
        </p>
      </section>
    </main>
  );
}
