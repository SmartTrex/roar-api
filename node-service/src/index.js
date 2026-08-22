import express from "express";
import helmet from "helmet";
import rateLimit from "express-rate-limit";

const app = express();

const port = process.env.API_PORT || 3000;
const publicApiKey = process.env.PUBLIC_API_KEY;
const libreTranslateUrl = process.env.LIBRETRANSLATE_URL || "http://libretranslate:5000";

app.use(helmet());
app.use(express.json({ limit: "10kb" }));

const translateLimiter = rateLimit({
  windowMs: 60 * 1000,
  limit: 60,
  standardHeaders: true,
  legacyHeaders: false
});

function requireApiKey(req, res, next) {
  const apiKey = req.header("X-Smart-Trex-Key");

  if (!publicApiKey || apiKey !== publicApiKey) {
    return res.status(401).json({
      error: "Unauthorized"
    });
  }

  next();
}

app.get("/health", (_, res) => {
  res.json({
    status: "ok"
  });
});

app.get("/api/languages", requireApiKey, async (_, res) => {
  try {
    const response = await fetch(`${libreTranslateUrl}/languages`);
    const data = await response.json();

    if (!response.ok) {
      return res.status(response.status).json({
        error: "Translation provider error",
        details: data
      });
    }

    return res.json(data);
  } catch (error) {
    console.error(error);

    return res.status(500).json({
      error: "Internal server error"
    });
  }
});

app.post("/api/translate", requireApiKey, translateLimiter, async (req, res) => {
  try {
    const { text, source = "auto", target = "ru" } = req.body;

    if (!text || typeof text !== "string") {
      return res.status(400).json({
        error: "Text is required"
      });
    }

    const normalizedText = text.trim();

    if (normalizedText.length === 0) {
      return res.status(400).json({
        error: "Text is empty"
      });
    }

    if (normalizedText.length > 1000) {
      return res.status(400).json({
        error: "Text is too long"
      });
    }

    const response = await fetch(`${libreTranslateUrl}/translate`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        q: normalizedText,
        source,
        target,
        format: "text"
      })
    });

    const data = await response.json();

    if (!response.ok) {
      return res.status(response.status).json({
        error: "Translation provider error",
        details: data
      });
    }

    return res.json({
      originalText: normalizedText,
      translatedText: data.translatedText,
      source,
      target
    });
  } catch (error) {
    console.error(error);

    return res.status(500).json({
      error: "Internal server error"
    });
  }
});

const supportedBookLanguages = new Set(["en", "es", "fr", "de", "pt"]);

function getBookAuthor(book) {
  const author = book.authors?.[0];

  return author?.name || "Unknown author";
}

function getBookCoverUrl(book) {
  return book.formats?.["image/jpeg"] || null;
}

function getBookDownloadUrl(book) {
  const formats = book.formats || {};

  return (
    formats["application/epub+zip"] ||
    formats["application/epub+zip; charset=utf-8"] ||
    null
  );
}

app.get("/api/books", requireApiKey, async (req, res) => {
  try {
    const language = typeof req.query.language === "string"
      ? req.query.language.toLowerCase()
      : "en";

    const rawPage = typeof req.query.page === "string"
      ? req.query.page
      : "1";

    const page = Number.parseInt(rawPage, 10);

    if (!supportedBookLanguages.has(language)) {
      return res.status(400).json({
        error: "Unsupported language"
      });
    }

    if (!Number.isInteger(page) || page < 1) {
      return res.status(400).json({
        error: "Invalid page"
      });
    }

    const gutendexURL = new URL("https://gutendex.com/books");
    gutendexURL.searchParams.set("languages", language);
    gutendexURL.searchParams.set("copyright", "false");
    gutendexURL.searchParams.set("page", String(page));

    const response = await fetch(gutendexURL);

    if (!response.ok) {
      return res.status(502).json({
        error: "Book catalog provider error"
      });
    }

    const data = await response.json();

    const books = data.results
      .map((book) => ({
        id: book.id,
        title: book.title,
        author: getBookAuthor(book),
        language: book.languages?.[0] || language,
        coverUrl: getBookCoverUrl(book),
        downloadUrl: getBookDownloadUrl(book)
      }))
      .filter((book) => book.downloadUrl);

    return res.json({
      page,
      count: data.count,
      hasNextPage: Boolean(data.next),
      books
    });
  } catch (error) {
    console.error(error);

    return res.status(500).json({
      error: "Internal server error"
    });
  }
});

app.listen(port, () => {
  console.log(`Smart-Trex API started on port ${port}`);
});
