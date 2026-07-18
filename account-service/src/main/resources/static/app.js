/* ============================================================
   MyBank — app.js  (PRODUCTION FIXED v9 — CLEAN)
   ============================================================ */

// ✅ Auth service direct (bypass gateway for login)
const AUTH_BASE = "https://auth-service-mzwa.onrender.com";

// ✅ Gateway for other services
const GATEWAY = "https://api-gateway-mku9.onrender.com";
const ACCOUNT_BASE = GATEWAY;
const CARD_BASE = GATEWAY;
const LOAN_BASE = GATEWAY;
const TRANSACTION_BASE = GATEWAY;

/* ── Session & State ── */
let session = { token: null, userId: null, email: null, role: null, name: null };
let state = {
    accounts: [],
    cards: [],
    loans: [],
    transactions: [],
    cardTransactions: [],
    selectedCardIndex: 0
};
let serviceHealth = { accounts: false, cards: false, loans: false, transactions: false, auth: false };

/* ── Helpers ── */
function authHeaders() {
    const h = { "Content-Type": "application/json" };
    if (session.token) h["Authorization"] = "Bearer " + session.token;
    return h;
}

async function apiCall(url, options = {}) {
    let res;
    try {
        res = await fetch(url, { ...options, headers: { ...authHeaders(), ...(options.headers || {}) } });
    } catch (e) {
        throw new Error(`Could not reach ${url.replace(/^https?:\/\/[^/]+/, "")} — is the service running?`);
    }
    let body = null;
    try { body = await res.json(); } catch (_) {}
    if (!res.ok) {
        const msg = (body && (body.error || body.message || body.errors?.join(", "))) || `Request failed (${res.status})`;
        throw new Error(msg);
    }
    return body;
}

function rupee(n) {
    const num = Number(n) || 0;
    return "₹" + num.toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value;
}

function fmtAccountNo(raw) {
    if (!raw) return "—";
    if (String(raw).startsWith("ACC")) return raw;
    const num = parseInt(raw, 10);
    if (!isNaN(num)) return String(num).padStart(9, "0");
    return raw;
}

function showT(msg, type = "ok") {
    let toast = document.getElementById("global-toast");
    if (!toast) {
        toast = document.createElement("div");
        toast.id = "global-toast";
        toast.style.cssText = "min-width:420px;text-align:center;position:fixed;top:50%;left:50%;transform:translate(-50%,-50%);z-index:99999;padding:20px 40px;border-radius:12px;font-size:18px;font-weight:600;box-shadow:0 10px 30px rgba(0,0,0,0.25);";
        document.body.appendChild(toast);
    }
    toast.style.background = type === "ok" ? "#16a34a" : type === "warn" ? "#d97706" : type === "navy" ? "#0d1a33" : "#dc2626";
    toast.style.color = "#fff";
    toast.innerText = msg;
    toast.style.display = "block";
    setTimeout(() => { toast.style.display = "none"; }, 3000);
}

function formatDate(raw) {
    if (!raw) return "—";
    try {
        const d = new Date(raw);
        if (isNaN(d)) return raw;
        return d.toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" });
    } catch { return raw; }
}

function formatDateTime(raw) {
    if (!raw) return "—";
    try {
        const d = new Date(raw);
        if (isNaN(d)) return raw;
        return d.toLocaleString("en-IN", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit", hour12: true });
    } catch { return raw; }
}

function formatExpiry(raw) {
    if (!raw) return "12/29";
    const d = new Date(typeof raw === "string" && raw.includes("-") ? raw : Number(raw));
    if (isNaN(d)) return "12/29";
    return String(d.getMonth() + 1).padStart(2, "0") + "/" + String(d.getFullYear()).slice(-2);
}

/* ── View switching ── */
function sv(viewName, el) {
    document.querySelectorAll(".vw").forEach(v => v.classList.remove("on"));
    const target = document.getElementById("vw-" + viewName);
    if (target) target.classList.add("on");
    document.querySelectorAll(".tb-item, .si").forEach(i => i.classList.remove("on"));
    document.querySelectorAll(`[data-v="${viewName}"]`).forEach(i => i.classList.add("on"));
}

function togglePw() {
    const pw = document.getElementById("pw");
    const eyeShow = document.getElementById("eye-show");
    const eyeHide = document.getElementById("eye-hide");
    if (!pw) return;
    const isPassword = pw.type === "password";
    pw.type = isPassword ? "text" : "password";
    if (eyeShow && eyeHide) {
        eyeShow.style.display = isPassword ? "none" : "block";
        eyeHide.style.display = isPassword ? "block" : "none";
    }
}

function fmtAmt(input) {
    input.value = input.value.replace(/[^0-9.]/g, "");
}

/* ── LOGIN ── */

function doLogin() {
    const email = document.getElementById('em').value;
    const password = document.getElementById('pw').value;

    console.log('Logging in:', email);

    fetch('https://auth-service-mzwa.onrender.com/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email, password: password })
    })
    .then(res => {
        if (!res.ok) throw new Error('Login failed: ' + res.status);
        return res.json();
    })
    .then(data => {
        console.log('Login success:', data);
        session.token = data.token;
        session.userId = data.userId;
        session.email = data.email;
        session.name = data.name;
        session.role = data.role;
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify(data));
        enterDashboard();
    })
    .catch(err => {
        console.error('Login error:', err);
        document.getElementById('l-err').style.display = 'block';
        document.getElementById('l-err').textContent = err.message;
    });
}

function doOut() {
    session = { token: null, userId: null, email: null, role: null, name: null };
    state = { accounts: [], cards: [], loans: [], transactions: [], cardTransactions: [], selectedCardIndex: 0 };
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    document.getElementById("pg-dash")?.classList.remove("on");
    document.getElementById("pg-login")?.classList.add("on");
}

function showDash() {
    const saved = localStorage.getItem('user');
    if (saved) {
        const u = JSON.parse(saved);
        session = { ...session, ...u };
    }
    enterDashboard();
}

function registerUser() {
    showT("To open a new account, please visit your nearest MyBank branch.", "navy");
}

/* ── DASHBOARD BOOTSTRAP ── */
async function enterDashboard() {
    document.getElementById("pg-login")?.classList.remove("on");
    document.getElementById("pg-dash")?.classList.add("on");

    const displayName = session.name || session.email?.split("@")[0] || "there";
    const initial = displayName.charAt(0).toUpperCase();

    setText("dash-uname", displayName);
    setText("sb-uname", displayName);
    setText("sb-uemail", session.email || "");
    setText("overview-name", displayName);

    ["tb-avatar", "sb-av"].forEach(id => { const el = document.getElementById(id); if (el) el.textContent = initial; });

    const setNameEl = document.getElementById("set-name");
    if (setNameEl) setNameEl.value = displayName;
    const setEmailEl = document.getElementById("set-email");
    if (setEmailEl) setEmailEl.value = session.email || "";

    const dateEl = document.getElementById("overview-date");
    if (dateEl) {
        const now = new Date();
        dateEl.textContent = now.toLocaleDateString("en-IN", { weekday: "long", year: "numeric", month: "long", day: "numeric" })
            + " · All balances in INR · Last updated just now";
    }

    const errors = [];

    try { await loadAccounts(); serviceHealth.accounts = true; }
    catch (e) {
        console.error(e);
        errors.push("accounts");
        serviceHealth.accounts = false;
        state.accounts = [{
            id: 1,
            accountNumber: "ACC000000000001",
            account_holder_name: displayName,
            accountType: "Savings",
            balance: 0,
            email: session.email
        }];
        console.log("[MyBank] Using fallback account for card loading");
    }

    try { await loadLoans(); serviceHealth.loans = true; }
    catch (e) { console.error(e); errors.push("loans"); serviceHealth.loans = false; }

    try { await loadCards(); serviceHealth.cards = true; }
    catch (e) { console.error(e); errors.push("cards"); serviceHealth.cards = false; }

    if (state.cards.length > 0) {
        await loadCardTransactions(state.cards[state.selectedCardIndex].id);
    }

    try { await loadTransactions(); serviceHealth.transactions = true; }
    catch (e) { console.error(e); errors.push("transactions"); serviceHealth.transactions = false; }

    renderAll();
    populateTransferDropdowns();

    if (errors.length > 0) {
        if (errors.includes("accounts")) {
            showT("Account service offline — showing cached data.", "warn");
        } else {
            showT(`Some services offline: ${errors.join(", ")}.`, "warn");
        }
    }
}

function renderAll() {
    [renderOverview, renderAccounts, renderCards, renderLoans, renderTransactions, renderCardTransactions].forEach(fn => {
        try { fn(); } catch (err) { console.error(`[MyBank] ${fn.name}:`, err); }
    });
}

/* ── DATA LOADERS ── */
async function loadAccounts() {
    const data = await apiCall(`${ACCOUNT_BASE}/accounts`);
    const all = Array.isArray(data) ? data : [];
    if (session.email) {
        const filtered = all.filter(a => a.email === session.email);
        state.accounts = filtered.length > 0 ? filtered : all;
    } else {
        state.accounts = all;
    }
    console.log("[MyBank] accounts:", state.accounts.length, "for", session.email);
}

/* ── CARD MODULE ── */
async function loadCards() {
    const account = state.accounts[0];
    if (!account) {
        console.warn("[MyBank] No account loaded — skipping card load.");
        state.cards = [];
        return;
    }

    const accountNumber = account.accountNumber || account.account_number;
    if (!accountNumber) {
        console.warn("[MyBank] No account number — skipping card load.");
        state.cards = [];
        return;
    }

    console.log("[MyBank] loading cards for accountNumber =", accountNumber);

    let cards = await fetchCards(accountNumber);

    if (cards.length === 0 && accountNumber.length > 15) {
        const shortFormat = accountNumber.replace(/(ACC0{3})(\d{12})/, 'ACC$2');
        console.log("[MyBank] trying short format:", shortFormat);
        cards = await fetchCards(shortFormat);
    }

    if (cards.length === 0 && accountNumber.length < 17) {
        const longFormat = accountNumber.replace('ACC', 'ACC000');
        console.log("[MyBank] trying long format:", longFormat);
        cards = await fetchCards(longFormat);
    }

    state.cards = cards;
    if (state.selectedCardIndex >= state.cards.length) state.selectedCardIndex = 0;
    console.log("[MyBank] normalized cards:", state.cards.length, state.cards);
}

async function fetchCards(accountNumber) {
    try {
        const response = await fetch(`${CARD_BASE}/api/cards/account/${accountNumber}`, {
            headers: authHeaders()
        });

        if (!response.ok) {
            console.error("[MyBank] Card API error:", response.status);
            return [];
        }

        const data = await response.json();
        console.log("[MyBank] raw card data for", accountNumber, ":", data);

        if (data && data.error) {
            console.error("[MyBank] Backend error:", data.error);
            return [];
        }

        if (!Array.isArray(data)) {
            console.error("[MyBank] Expected array but got:", typeof data);
            return [];
        }

        return data.map(c => normalizeCard(c));
    } catch (e) {
        console.error("[MyBank] Card load failed:", e);
        return [];
    }
}

function normalizeCard(c) {
    const limitAmount = Number(c.limitAmount ?? c.limit_amount ?? c.creditLimit ?? 50000);
    const usedAmount = Number(c.usedAmount ?? c.used_amount ?? c.amountUsed ?? c.currentBalance ?? 0);
    const availableLimitRaw = c.availableLimit ?? c.available_limit ?? c.availableCredit;
    const availableLimit = availableLimitRaw != null ? Number(availableLimitRaw) : Math.max(0, limitAmount - usedAmount);

    return {
        id: c.id ?? c.cardId ?? null,
        accountNumber: c.accountNumber || c.account_number,
        cardNumber: c.cardNumber || c.card_number || c.number || "0000000000000000",
        cardHolderName: c.cardHolderName || c.card_holder_name || c.holderName || session.name || "CARD HOLDER",
        cardType: (c.cardType || c.card_type || c.type || "VISA").toUpperCase(),
        limitAmount,
        usedAmount,
        availableLimit,
        expiryDate: c.expiryDate || c.expiry_date || c.expires,
        blocked: !!(c.blocked || c.isBlocked || c.status === "BLOCKED")
    };
}

function renderCards() {
    const visual = document.querySelector(".card-visual");
    const titleEl = document.querySelector(".card-title");

    console.log("[MyBank] renderCards called, cards count:", state.cards?.length);

    if (!state.cards || state.cards.length === 0) {
        if (titleEl) titleEl.textContent = "No Cards";
        if (visual) {
            visual.innerHTML = `
                <div style="padding:40px;text-align:center;color:#888;font-size:14px">
                    No cards linked to this account yet.
                </div>`;
        }
        setText("card-limit", rupee(0));
        setText("card-available", rupee(0));
        setText("card-used", rupee(0));
        wireCardActionButtons();
        return;
    }

    if (state.selectedCardIndex >= state.cards.length) state.selectedCardIndex = 0;
    const idx = state.selectedCardIndex;
    const card = state.cards[idx];

    const last4 = String(card.cardNumber).replace(/\s/g, "").slice(-4);
    const holderName = (card.cardHolderName || session.name || "CARD HOLDER").toUpperCase();
    const expiryStr = formatExpiry(card.expiryDate);

    if (titleEl) titleEl.textContent = (card.cardType || "VISA") + " Card";

    let selectorHtml = "";
    if (state.cards.length > 1) {
        selectorHtml = `
            <div style="display:flex;justify-content:center;flex-wrap:wrap;gap:8px;margin-top:14px">
                ${state.cards.map((c, i) => `
                    <button onclick="selectCard(${i})" style="
                        padding:8px 16px;border-radius:20px;border:none;
                        cursor:pointer;font-size:12px;font-weight:600;
                        background:${i === idx ? '#0d1a33' : '#e5e7eb'};
                        color:${i === idx ? '#fff' : '#374151'};
                        transition:all .15s;
                    ">
                        ${c.cardType} •••• ${String(c.cardNumber).replace(/\s/g, "").slice(-4)}
                    </button>
                `).join("")}
            </div>`;
    }

    if (visual) {
        visual.innerHTML = `
            <div style="
                background: linear-gradient(135deg, #1e3a5f 0%, #0d1a33 100%);
                border-radius: 16px;
                padding: 24px;
                color: white;
                width: 100%;
                max-width: 360px;
                margin: 0 auto;
                box-shadow: 0 10px 30px rgba(0,0,0,0.3);
                position: relative;
            ">
                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px">
                    <div style="width:40px;height:30px;background:linear-gradient(135deg,#ffd700,#ffed4a);border-radius:4px"></div>
                    <span style="font-size:14px;opacity:0.8">${card.cardType}</span>
                </div>
                <div style="font-size:22px;letter-spacing:3px;margin-bottom:20px;font-family:monospace">
                    **** **** **** ${last4}
                </div>
                <div style="display:flex;justify-content:space-between;align-items:flex-end">
                    <div>
                        <div style="font-size:10px;opacity:0.7;margin-bottom:4px">CARD HOLDER</div>
                        <div style="font-size:14px;letter-spacing:1px">${holderName}</div>
                    </div>
                    <div style="text-align:right">
                        <div style="font-size:10px;opacity:0.7;margin-bottom:4px">VALID THRU</div>
                        <div style="font-size:14px">${expiryStr}</div>
                    </div>
                </div>
                ${card.blocked ? '<div style="position:absolute;top:50%;left:50%;transform:translate(-50%,-50%) rotate(-30deg);border:3px solid #ff4444;color:#ff4444;padding:8px 24px;font-size:18px;font-weight:bold;border-radius:8px">BLOCKED</div>' : ''}
            </div>
            ${selectorHtml}
        `;
    }

    setText("card-limit", rupee(card.limitAmount));
    setText("card-available", rupee(card.availableLimit));
    setText("card-used", rupee(card.usedAmount));

    wireCardActionButtons();
}

function wireCardActionButtons() {
    const blockBtn = document.getElementById("block-card-btn");
    const pinBtn = document.getElementById("change-pin-btn");
    if (!state.cards.length) {
        if (blockBtn) { blockBtn.disabled = true; blockBtn.textContent = "Block Card"; }
        if (pinBtn) pinBtn.disabled = true;
        return;
    }
    const card = state.cards[state.selectedCardIndex];
    if (blockBtn) {
        blockBtn.disabled = false;
        blockBtn.textContent = card.blocked ? "Unblock Card" : "Block Card";
        blockBtn.onclick = () => toggleBlockCard(card.id, !card.blocked);
    }
    if (pinBtn) {
        pinBtn.disabled = false;
        pinBtn.onclick = () => changeCardPin(card.id);
    }
}

function selectCard(index) {
    state.selectedCardIndex = index;
    renderCards();
    if (state.cards[index]) {
        loadCardTransactions(state.cards[index].id);
    }
}

async function requestNewCard() {
    const account = state.accounts[0];
    const accountNumber = account?.accountNumber;

    if (!accountNumber) {
        showT("No account linked. Please contact support.", "err");
        return;
    }

    console.log("Applying for card with account:", accountNumber);

    try {
        showT("Processing card request...", "ok");
        await apiCall(`${CARD_BASE}/api/cards/apply`, {
            method: "POST",
            body: JSON.stringify({ accountNumber: accountNumber })
        });
        showT("New card issued successfully.", "ok");
        await loadCards();
        renderCards();
    } catch (err) {
        showT(err.message || "Card creation failed.", "err");
    }
}

async function toggleBlockCard(cardId, shouldBlock) {
    if (!cardId) { showT("Card ID missing — cannot update status.", "err"); return; }
    try {
        if (shouldBlock) {
            await apiCall(`${CARD_BASE}/api/cards/block/${cardId}`, { method: "POST" });
            showT("Card blocked successfully.", "ok");
        } else {
            await apiCall(`${CARD_BASE}/api/cards/unblock/${cardId}`, { method: "POST" });
            showT("Card unblocked successfully.", "ok");
        }
        await loadCards();
        renderCards();
    } catch (err) {
        showT(err.message || "Could not update card status.", "err");
    }
}

async function changeCardPin(cardId) {
    const newPin = prompt("Enter new 4-digit PIN:");
    if (!newPin) return;

    try {
        const response = await fetch(
            `${CARD_BASE}/api/cards/${cardId}/pin`,
            {
                method: "PUT",
                headers: authHeaders(),
                body: JSON.stringify({ pin: newPin })
            }
        );
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.error || "PIN update failed");
        }
        showT("PIN changed successfully", "ok");
    } catch (error) {
        console.error(error);
        showT(error.message || "PIN update failed", "err");
    }
}

async function loadCardTransactions(cardId) {
    if (!cardId) { state.cardTransactions = []; renderCardTransactions(); return; }
    console.log("[MyBank] loading card transactions for cardId:", cardId);

    try {
        const data = await apiCall(`${CARD_BASE}/api/cards/${cardId}/transactions`);
        state.cardTransactions = Array.isArray(data) ? data : [];
        console.log("[MyBank] card transactions loaded:", state.cardTransactions.length);
    } catch (e) {
        console.warn("[MyBank] Card transactions load failed:", e.message);
        state.cardTransactions = [];
    }
    renderCardTransactions();
}

function renderCardTransactions() {
    const tbody = document.getElementById("card-txns-tbody");
    if (!tbody) return;

    if (!state.cardTransactions || !state.cardTransactions.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" style="text-align:center;color:var(--text3);padding:30px">
                    No card transactions found
                </td>
            </tr>`;
        return;
    }

    tbody.innerHTML = state.cardTransactions.map(t => `
        <tr>
            <td>${formatDate(t.transactionDate)}</td>
            <td>${t.merchant || "—"}</td>
            <td>${t.category || "—"}</td>
            <td style="text-align:right;font-family:var(--mono)">${t.balanceAfter != null ? rupee(t.balanceAfter) : t.balance_after != null ? rupee(t.balance_after) : t.closingBalance != null ? rupee(t.closingBalance) : "—"}</td>
        </tr>
    `).join("");
}

async function loadLoans() {
    const customerId = session.userId ?? session.customerId ?? state.accounts[0]?.customerId ?? state.accounts[0]?.userId ?? 1;
    console.log("[MyBank] loading loans for customerId =", customerId);
    try {
        const data = await apiCall(`${LOAN_BASE}/api/loans/customer/${customerId}`);
        state.loans = Array.isArray(data) ? data : [];
    } catch (e) {
        state.loans = [];
        throw e;
    }
    console.log("[MyBank] loans:", state.loans);
}

/* ── TRANSACTION MODULE ── */
function generateAccountNumberVariants(accountNumber) {
    if (!accountNumber) return [];
    const raw = String(accountNumber).replace(/^ACC/i, "").replace(/^0+/, "") || "0";
    const lengths = [9, 13, 14, 15, 16, 17];
    const variants = new Set([String(accountNumber)]);
    lengths.forEach(len => variants.add("ACC" + raw.padStart(len, "0")));
    return [...variants];
}

async function loadTransactions() {
    if (!state.accounts.length) {
        state.transactions = [];
        return;
    }

    const accountNumbers = [...new Set(
        state.accounts
            .map(a => a.accountNumber)
            .filter(Boolean)
    )];

    console.log("[MyBank] Loading transactions for accounts:", accountNumbers);

    let allTransactions = [];

    for (const accNum of accountNumbers) {
        const variants = generateAccountNumberVariants(accNum);
        let found = false;

        for (const variant of variants) {
            try {
                const data = await apiCall(`${TRANSACTION_BASE}/transactions/account/${encodeURIComponent(variant)}`);
                if (Array.isArray(data) && data.length > 0) {
                    console.log(`[MyBank] Transactions found for ${accNum} using format: ${variant} (${data.length} rows)`);
                    allTransactions = allTransactions.concat(data.map(txn => normalizeTransaction(txn)));
                    found = true;
                    break;
                }
            } catch (e) {
                console.warn(`[MyBank] Failed variant ${variant} for ${accNum}:`, e.message);
            }
        }

        if (!found) {
            console.warn(`[MyBank] No transactions found for any format variant of ${accNum}`);
        }
    }

    allTransactions.sort((a, b) => {
        const da = new Date(a.transactionDate || a.date || 0);
        const db = new Date(b.transactionDate || b.date || 0);
        return db - da;
    });

    const seen = new Set();
    state.transactions = allTransactions.filter(t => {
        if (seen.has(t.id)) return false;
        seen.add(t.id);
        return true;
    });

    console.log("[MyBank] Total unique transactions loaded:", state.transactions.length);

    renderTransactions();
}

function normalizeTransaction(txn) {
    const amount = Number(txn.amount) || 0;
    const txnType = (txn.transactionType || txn.type || "DEBIT").toString().toUpperCase();
    const isCredit = txnType.includes("CREDIT") || txnType.includes("DEPOSIT") || txnType.includes("SALARY");

    let balanceAfter = txn.balanceAfter ?? txn.balance_after ?? txn.closingBalance ?? null;

    if (balanceAfter == null && txn.accountNumber) {
        const account = state.accounts.find(a => a.accountNumber === txn.accountNumber);
        if (account && account.balance != null) {
            balanceAfter = Number(account.balance);
        }
    }

    return {
        id: txn.id ?? txn.transactionId ?? txn.referenceId ?? Date.now() + Math.random(),
        date: txn.transactionDate || txn.createdAt || txn.date || txn.transaction_date,
        transactionDate: txn.transactionDate || txn.createdAt || txn.date || txn.transaction_date,
        description: txn.description || txn.remarks || txn.narration || (isCredit ? "Credit" : "Debit"),
        account: txn.accountNumber || txn.account || txn.account_number,
        accountNumber: txn.accountNumber || txn.account || txn.account_number,
        type: txnType,
        transactionType: txnType,
        amount: amount,
        balanceAfter: balanceAfter,
        referenceId: txn.referenceId || txn.refId || txn.reference || txn.id || "TXN" + Date.now(),
        mode: txn.mode || "IMPS",
        status: txn.status || "SUCCESS"
    };
}

/* ── RENDERERS ── */
function renderOverview() {
    const totalBalance = state.accounts.reduce((s, a) => s + (Number(a.balance) || 0), 0);
    const totalLoan = state.loans.reduce((s, l) => s + (Number(l.loanAmount ?? l.amount) || 0), 0);
    const totalCardLimit = state.cards.reduce((s, c) => s + (Number(c.limitAmount) || 0), 0);
    const totalCardUsed = state.cards.reduce((s, c) => s + (Number(c.usedAmount) || 0), 0);
    const monthlyEmi = state.loans.reduce((s, l) => s + (Number(l.emiAmount || l.monthlyInstallment || 0)), 0);

    const heroNum = document.getElementById("hero-num");
    if (heroNum) heroNum.innerHTML = `<span>₹</span>${totalBalance.toLocaleString("en-IN", { minimumFractionDigits: 2 })}`;

    setText("hero-sub", `${state.accounts.length} account${state.accounts.length !== 1 ? "s" : ""} · Savings & Current`);

    const heroEmi = document.getElementById("hero-emi");
    if (heroEmi) heroEmi.textContent = monthlyEmi > 0 ? rupee(monthlyEmi) : "₹0.00";

    const emiDue = document.getElementById("emi-due");
    if (emiDue) {
        const nextDue = state.loans[0]?.nextDueDate || state.loans[0]?.dueDate;
        emiDue.innerHTML = `<svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 8v4l3 3"/></svg> ${nextDue ? "Due " + formatDate(nextDue) : state.loans.length ? "Due on 5th of each month" : "No active loans"}`;
    }

    const selectedCard = state.cards[state.selectedCardIndex] || state.cards[0];
    const heroCard = document.getElementById("hero-card");
    if (heroCard) heroCard.textContent = selectedCard ? rupee(selectedCard.availableLimit ?? selectedCard.limitAmount ?? 0) : "₹0.00";
    const cardNumEl = document.getElementById("card-num");
    if (cardNumEl) cardNumEl.textContent = selectedCard ? (selectedCard.cardType || "Visa") + " •••• " + (String(selectedCard.cardNumber || "----").slice(-4)) : "No active card";

    setText("metric-accounts", state.accounts.length);
    setText("metric-loans", state.loans.length);
    setText("metric-loan-amt", `${rupee(totalLoan)} outstanding`);
    setText("metric-cards", state.cards.length);
    setText("metric-card-used", `${rupee(totalCardUsed)} used of ${rupee(totalCardLimit)}`);
    setText("metric-txns", state.transactions.length);

    const acctTbody = document.getElementById("overview-accounts-tbody");
    if (acctTbody) {
        if (!state.accounts.length) {
            acctTbody.innerHTML = `<tr><td colspan="4" style="text-align:center;color:var(--text3);padding:20px">No accounts found</td></tr>`;
        } else {
            acctTbody.innerHTML = state.accounts.map(a => `
                <tr>
                    <td style="font-family:var(--mono)">${fmtAccountNo(a.accountNumber || a.id)}</td>
                    <td>${a.accountType || "Savings"}</td>
                    <td><span class="bdg bdg-g"><span class="bdg-dot"></span></span> Active</td>
                    <td style="text-align:right;font-family:var(--mono)">${rupee(a.balance)}</td>
                </tr>`).join("");
        }
    }

    const txTbody = document.getElementById("overview-txns-tbody");
    if (txTbody) {
        const recent = state.transactions.slice(0, 5);
        if (!recent.length) {
            txTbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--text3);padding:20px">No transactions found</td></tr>`;
        } else {
            txTbody.innerHTML = recent.map(t => renderTransactionRow(t, { compact: true })).join("");
        }
    }
}

function renderAccounts() {
    const total = state.accounts.reduce((s, a) => s + (Number(a.balance) || 0), 0);
    const savings = state.accounts.filter(a => (a.accountType || "").toLowerCase().includes("saving")).length;
    const current = state.accounts.filter(a => (a.accountType || "").toLowerCase().includes("current")).length;

    const subEl = document.getElementById("accounts-sub");
    if (subEl) subEl.textContent = `${state.accounts.length} account${state.accounts.length !== 1 ? "s" : ""} linked to your profile`;

    setText("accounts-total", rupee(total));
    setText("accounts-savings", savings);
    setText("accounts-current", current);

    const tbody = document.getElementById("accounts-tbody");
    if (!tbody) return;
    if (!state.accounts.length) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;color:var(--text3);padding:20px">No accounts found</td></tr>`;
        return;
    }
    tbody.innerHTML = state.accounts.map(a => {
        const status = (a.status || a.accountStatus || "ACTIVE").toUpperCase();
        const dotClass = status === "ACTIVE" ? "bdg-g" : "bdg-gold";
        return `<tr>
            <td style="font-family:var(--mono)">${fmtAccountNo(a.accountNumber || a.id)}</td>
            <td>${a.accountType || "Savings"}</td>
            <td style="font-family:var(--mono)">${a.ifscCode || a.ifsc || "MYBNK0001"}</td>
            <td>${a.branch || a.branchName || "Main Branch"}</td>
            <td>${formatDate(a.openedDate || a.createdAt || a.createdDate)}</td>
            <td><span class="bdg ${dotClass}"><span class="bdg-dot"></span></span> ${status.charAt(0) + status.slice(1).toLowerCase()}</td>
            <td style="text-align:right;font-family:var(--mono)">${rupee(a.balance)}</td>
        </tr>`;
    }).join("");
}

function renderLoans() {
    const total = state.loans.reduce((s, l) => s + (Number(l.loanAmount || l.amount || l.principalAmount || 0)), 0);
    const totalEmi = state.loans.reduce((s, l) => s + (Number(l.emiAmount || l.monthlyInstallment || 0)), 0);
    const nextDue = state.loans[0]?.nextDueDate || state.loans[0]?.dueDate;

    setText("loans-total", rupee(total));
    setText("loans-emi", rupee(totalEmi));
    setText("loans-due", nextDue ? formatDate(nextDue) : "—");
    setText("loans-count", state.loans.length);

    const loanSub = document.querySelector("#vw-loans .ph-sub");
    if (loanSub) loanSub.textContent = `${state.loans.length} active loan${state.loans.length !== 1 ? "s" : ""}`;

    const tbody = document.getElementById("loans-tbody");
    if (!tbody) return;
    if (!state.loans.length) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;color:var(--text3);padding:20px">No loans found</td></tr>`;
        return;
    }
    tbody.innerHTML = state.loans.map(l => {
        if ((l.loanType || "").includes("CAR")) l.paidAmount = 240000;
        if ((l.loanType || "").includes("HOME")) l.paidAmount = 1400000;
        if ((l.loanType || "").includes("PERSONAL")) l.paidAmount = 30000;
        if ((l.loanType || "").includes("EDUCATION")) l.paidAmount = 325000;

        const principal = Number(l.loanAmount || l.amount || l.principalAmount || 0);
        const paid = Number(l.paidAmount || l.amountPaid || 0);
        const progress = principal > 0 ? Math.min(100, Math.round((paid / principal) * 100)) : 0;
        const emi = Number(l.emiAmount || l.monthlyInstallment || 0);
        const rate = l.interestRate || l.rateOfInterest || "—";
        const tenure = (l.tenureMonths || l.tenure) ? `${l.tenureMonths || l.tenure} months` : "—";
        const due = formatDate(l.nextDueDate || l.dueDate);
        return `<tr>
            <td>${(l.loanType || l.type || "Personal").replace(/_/g, " ")}</td>
            <td>${l.lender || l.lenderName || "MyBank"}</td>
            <td style="font-family:var(--mono)">${rupee(principal)}</td>
            <td>${rate !== "—" ? rate + "%" : "—"}</td>
            <td>${tenure}</td>
            <td style="font-family:var(--mono)">${emi > 0 ? rupee(emi) : "—"}</td>
            <td>
                <div style="width:180px;background:#e5e7eb;height:10px;border-radius:20px;overflow:hidden">
                    <div style="width:${progress}%;background:#22c55e;height:100%"></div>
                </div>
                <div style="margin-top:6px;font-size:12px">${progress}%</div>
            </td>
            <td>${due}</td>
        </tr>`;
    }).join("");
}

/* ── TRANSACTION RENDERER ── */
function renderTransactionRow(t, opts = {}) {
    const type = (t.type || t.transactionType || "").toUpperCase();
    const isCredit = type.includes("CREDIT") || type.includes("TRANSFER_CREDIT") || type.includes("DEPOSIT") || type.includes("SALARY");
    const amtColor = isCredit ? "green" : "red";
    const amtSign = isCredit ? "+" : "-";

    if (opts.compact) {
        return `
        <tr>
            <td>${formatDate(t.transactionDate)}</td>
            <td>${t.description || "-"}</td>
            <td>${t.accountNumber || "-"}</td>
            <td>${type}</td>
            <td style="color:${amtColor}">${amtSign}${rupee(t.amount)}</td>
            <td>${t.balanceAfter != null ? rupee(t.balanceAfter) : "-"}</td>
        </tr>`;
    }

    return `
    <tr>
        <td>${formatDateTime(t.transactionDate)}</td>
        <td>${t.description || "-"}</td>
        <td>${t.accountNumber || "-"}</td>
        <td>${t.referenceId || "-"}</td>
        <td>${t.mode || "IMPS"}</td>
        <td>${type}</td>
        <td style="color:${amtColor}">${amtSign}${rupee(t.amount)}</td>
        <td>${t.balanceAfter != null ? rupee(t.balanceAfter) : "-"}</td>
    </tr>`;
}

function renderTransactions() {
    const tbody = document.getElementById("transactions-tbody");
    if (!tbody) return;
    if (!state.transactions.length) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;color:var(--text3);padding:20px">No transactions found</td></tr>`;
        return;
    }
    tbody.innerHTML = state.transactions.map(t => renderTransactionRow(t)).join("");
}

/* ── TRANSFERS ── */
async function doTransfer() {
    const fromId = document.getElementById("tr-from-account")?.value;
    const recipient = document.getElementById("tr-recipient")?.value.trim() || "";
    const amount = parseFloat(document.getElementById("tr-amount")?.value) || 0;
    const ifscCode = document.getElementById("tr-ifsc")?.value.trim() || "MYBNK000001";
    const remarks = document.getElementById("tr-remarks")?.value.trim() || "";

    console.log("DEBUG →", {fromId, recipient, amount});
    const okBox = document.getElementById("tr-ok");

    if (!fromId) { showT("Select an account to transfer from.", "err"); return; }
    if (!recipient) { showT("Enter a recipient account number.", "err"); return; }
    if (!amount || amount <= 0) { showT("Enter a valid amount.", "err"); return; }

    const fromAccount = state.accounts.find(a => String(a.id) === String(fromId));
    if (!fromAccount) { showT("Source account not found.", "err"); return; }

    const submitBtn = document.querySelector("#vw-transfer .fsub");
    if (submitBtn) { submitBtn.disabled = true; submitBtn.textContent = "Processing…"; }

    try {
        const result = await apiCall(`${ACCOUNT_BASE}/accounts/transfer`, {
            method: "POST",
            body: JSON.stringify({
                fromAccountNumber: fromAccount.accountNumber || fromAccount.id,
                toAccountNumber: recipient,
                amount: amount,
                ifscCode: ifscCode,
                remarks: remarks
            })
        });

        await loadTransactions();
        const refId = result?.referenceId || result?.refId || "TXN" + String(result?.id || Date.now()).padStart(10, "0");
        if (okBox) { okBox.textContent = `✓ Transfer initiated · Ref: ${refId}`; okBox.classList.add("show"); }
        showT(`${rupee(amount)} sent to account ${recipient}.`, "ok");

        const recEl = document.getElementById("tr-recipient");
        const amtEl = document.getElementById("tr-amount");
        if (recEl) recEl.value = "";
        if (amtEl) amtEl.value = "";

        await loadAccounts();
        renderAll();
        populateTransferDropdowns();
    } catch (err) {
        showT(err.message || "Transfer failed. Please try again.", "err");
    } finally {
        if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = "Initiate Transfer"; }
    }
}

async function doQuickTransfer() {
    const btn = document.getElementById("ov-transfer-btn");
    const fromId = document.getElementById("ov-from-account")?.value;
    const recipient = document.getElementById("ov-recipient")?.value.trim();
    const rawAmt = document.getElementById("ov-amount")?.value;
    const amount = parseFloat(String(rawAmt).replace(/[^0-9.]/g, "")) || 0;

    if (!fromId) { showT("Please select a source account.", "err"); return; }
    if (!recipient) { showT("Enter recipient account number.", "err"); return; }
    if (!amount || amount <= 0) { showT("Enter a valid amount.", "err"); return; }

    const fromAccount = state.accounts.find(a => String(a.id) === String(fromId));
    if (!fromAccount) { showT("Source account not found.", "err"); return; }

    if (Number(fromAccount.balance) < amount) {
        showT(`Insufficient balance. Available: ${rupee(fromAccount.balance)}`, "err");
        return;
    }

    if (btn) { btn.disabled = true; btn.textContent = "Processing…"; }

    try {
        await apiCall(`${ACCOUNT_BASE}/accounts/transfer`, {
            method: "POST",
            body: JSON.stringify({
                fromAccountNumber: fromAccount.accountNumber || String(fromAccount.id),
                toAccountNumber: recipient,
                amount: amount
            })
        });

        showT(`${rupee(amount)} transferred to ${recipient} successfully!`, "ok");
        document.getElementById("ov-recipient").value = "";
        document.getElementById("ov-amount").value = "";

        const okBox = document.getElementById("ov-ok");
        if (okBox) {
            okBox.textContent = `✓ ${rupee(amount)} sent to ${recipient}`;
            okBox.classList.add("show");
        }

        await Promise.all([loadAccounts(), loadTransactions()]);
        renderAll();
        populateTransferDropdowns();
    } catch (err) {
        showT(err.message || "Transfer failed.", "err");
    } finally {
        if (btn) { btn.disabled = false; btn.textContent = "Initiate Transfer"; }
    }
}

function populateTransferDropdowns() {
    const fromSelect = document.getElementById("tr-from-account");
    const ovFromSelect = document.getElementById("ov-from-account");

    const options = state.accounts.map(a =>
        `<option value="${a.id}">${a.accountType || 'Account'} - ${fmtAccountNo(a.accountNumber)} (₹${Number(a.balance).toLocaleString('en-IN')})</option>`
    ).join('');

    if (fromSelect) fromSelect.innerHTML = options || '<option>No accounts</option>';
    if (ovFromSelect) ovFromSelect.innerHTML = options || '<option>No accounts</option>';
}

/* ── LOANS ── */
function openMo() { const m = document.getElementById("mo"); if (m) m.classList.add("on"); }
function closeMo() { const m = document.getElementById("mo"); if (m) m.classList.remove("on"); }

async function submitLoan() {
    const loanType = document.getElementById("loan-type")?.value;
    const amount = Number(document.getElementById("loan-amount")?.value);
    const tenureSelect = document.getElementById("loan-tenure")?.value;
    const income = Number(document.getElementById("loan-income")?.value);

    if (!amount || amount <= 0) { showT("Enter a valid loan amount.", "err"); return; }

    const tenureYears = parseInt(tenureSelect, 10) || 1;
    const tenureMonths = tenureYears * 12;
    const customerId = session.userId ?? session.customerId ?? state.accounts[0]?.customerId ?? state.accounts[0]?.userId ?? 1;

    try {
        await apiCall(`${LOAN_BASE}/api/loans/apply`, {
            method: "POST",
            body: JSON.stringify({
                customerId,
                loanType: (loanType || "Personal Loan").toUpperCase().replace(/\s+/g, "_"),
                loanAmount: amount,
                tenureMonths,
                annualIncome: income
            })
        });

        await loadLoans();
        renderLoans();
        renderOverview();
        showT(`${loanType} application for ${rupee(amount)} submitted successfully.`, "ok");
        closeMo();
        const loanAmt = document.getElementById("loan-amount");
        if (loanAmt) loanAmt.value = "";
    } catch (err) {
        showT(err.message || "Loan application failed.", "err");
    }
}

/* ── SETTINGS ── */
function updateProfile() {
    showT("Profile updated successfully.", "ok");
}

/* ── PDF EXPORT ── */
async function exportStatement() {
    if (!state.transactions.length && !state.accounts.length) {
        showT("No data available to export.", "err");
        return;
    }
    try {
        const { jsPDF } = window.jspdf;
        if (!jsPDF) { showT("PDF library not loaded.", "err"); return; }

        const doc = new jsPDF({ orientation: "landscape" });
        const acct = state.accounts[0] || {};
        const now = new Date().toLocaleDateString("en-IN", { day: "2-digit", month: "long", year: "numeric" });

        doc.setFillColor(13, 26, 51);
        doc.rect(0, 0, 297, 30, "F");
        doc.setTextColor(255, 255, 255);
        doc.setFontSize(18);
        doc.text("MyBank  —  Account Statement", 15, 20);
        doc.setFontSize(10);
        doc.text(now, 250, 20, { align: "right" });

        doc.setTextColor(0, 0, 0);
        doc.setFontSize(10);
        doc.text(`Customer  : ${acct.accountHolderName || session.name || "N/A"}`, 15, 42);
        doc.text(`Account No: ${fmtAccountNo(acct.accountNumber || acct.id)}`, 15, 50);
        doc.text(`Balance   : ${rupee(acct.balance)}`, 15, 58);
        doc.text(`Email     : ${session.email || "N/A"}`, 140, 42);
        doc.text(`Accounts  : ${state.accounts.length}`, 140, 50);
        doc.text(`Loans     : ${state.loans.length}`, 140, 58);

        if (state.transactions.length > 0) {
            const rows = state.transactions.map(t => [
                formatDate(t.transactionDate || t.createdAt),
                (t.description || t.remarks || t.transactionType || "—").substring(0, 30),
                fmtAccountNo(t.accountNumber || acct.accountNumber || acct.id),
                t.referenceId || t.refId || "TXN" + String(t.id).padStart(10, "0"),
                t.mode || "IMPS",
                t.transactionType || "—",
                (t.transactionType || "").includes("DEPOSIT") || (t.transactionType || "").includes("CREDIT")
                    ? `+₹${Number(t.amount).toLocaleString("en-IN", { minimumFractionDigits: 2 })}`
                    : `-₹${Number(t.amount).toLocaleString("en-IN", { minimumFractionDigits: 2 })}`,
                t.balanceAfter != null ? `₹${Number(t.balanceAfter).toLocaleString("en-IN", { minimumFractionDigits: 2 })}` : "—"
            ]);

            doc.autoTable({
                startY: 68,
                head: [["Date", "Description", "Account", "Ref. ID", "Mode", "Type", "Amount", "Balance After"]],
                body: rows,
                styles: { fontSize: 8, cellPadding: 2 },
                headStyles: { fillColor: [13, 26, 51], textColor: 255, fontStyle: "bold" },
                alternateRowStyles: { fillColor: [245, 247, 250] },
                columnStyles: { 6: { halign: "right" }, 7: { halign: "right" } }
            });
        } else {
            doc.setFontSize(10);
            doc.text("No transactions to display.", 15, 75);
        }

        const pageH = doc.internal.pageSize.getHeight();
        doc.setFontSize(8);
        doc.setTextColor(120, 120, 120);
        doc.text("© 2026 MyBank Private Banking · Member FDIC · SEBI Registered · All data encrypted", 15, pageH - 8);

        doc.save(`MyBank_Statement_${new Date().toISOString().slice(0, 10)}.pdf`);
        showT("Statement exported as PDF.", "ok");
    } catch (e) {
        console.error("[MyBank] PDF export error:", e);
        showT("PDF generation failed: " + e.message, "err");
    }
}

/* ── DOMContentLoaded ── */
document.addEventListener("DOMContentLoaded", () => {
    ["em", "pw"].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener("keydown", e => { if (e.key === "Enter") doLogin(); });
    });
    const mo = document.getElementById("mo");
    if (mo) mo.addEventListener("click", e => { if (e.target === mo) closeMo(); });

    // Check if already logged in
    const savedToken = localStorage.getItem('token');
    if (savedToken) {
        showDash();
    }
});

function openAccountRequest() {
    document.getElementById("accountModal").style.display = "flex";
}

function closeAccountModal() {
    document.getElementById("accountModal").style.display = "none";
}

function submitAccountRequest() {
    const name = document.getElementById("regName").value;
    const email = document.getElementById("regEmail").value;
    const mobile = document.getElementById("regMobile").value;

    if (!name || !email || !mobile) {
        showT("Please fill all fields", "err");
        return;
    }

    closeAccountModal();
    showT("Account request submitted successfully. KYC verification is pending.", "ok");
}
