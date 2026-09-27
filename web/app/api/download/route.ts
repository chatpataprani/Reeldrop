import { NextResponse } from "next/server";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

function isAllowedUrl(value: string) {
  try {
    const u = new URL(value);
    return u.protocol === "https:" && /(^|\\.)instagram\\.com$/i.test(u.hostname);
  } catch {
    return false;
  }
}

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const url = typeof body?.url === "string" ? body.url.trim() : "";

    if (!url || !isAllowedUrl(url)) {
      return NextResponse.json(
        { status: "error", error: "Paste a valid https://www.instagram.com/... URL." },
        { status: 400 }
      );
    }

    const apiBase = (process.env.COBALT_API_URL || "https://api.cobalt.tools").replace(/\\/$/, "");
    const upstream = await fetch(apiBase + "/", {
      method: "POST",
      headers: { "Accept": "application/json", "Content-Type": "application/json" },
      body: JSON.stringify({
        url,
        downloadMode: "auto",
        videoQuality: "1080",
        filenameStyle: "pretty",
      }),
      cache: "no-store",
    });

    const data = await upstream.json().catch(() => ({}));

    if (!upstream.ok || data.status === "error") {
      const detail = data?.error?.code || data?.text || data?.message || upstream.statusText || "upstream downloader failed";
      return NextResponse.json({ status: "error", error: String(detail) }, { status: 502 });
    }

    if (data.status === "tunnel" || data.status === "redirect") {
      return NextResponse.json({
        status: "success",
        url: data.url,
        filename: data.filename || "yawr-download",
      });
    }

    if (data.status === "picker" && Array.isArray(data.picker) && data.picker[0]?.url) {
      return NextResponse.json({
        status: "success",
        url: data.picker[0].url,
        filename: "yawr-download",
      });
    }

    return NextResponse.json(
      { status: "error", error: "Downloader returned an unsupported response: " + JSON.stringify(data) },
      { status: 502 }
    );
  } catch (error) {
    return NextResponse.json(
      { status: "error", error: error instanceof Error ? error.message : "Unexpected server error" },
      { status: 500 }
    );
  }
}
