const apiBase = ""; // same origin

function statusSet(text) {
	document.getElementById("status").textContent = text;
}

async function login() {
	const id = parseInt(document.getElementById("userid").value, 10);
	const password = document.getElementById("password").value;

	const res = await fetch("/api/v1/auth/login", {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify({ id, password }),
	});

	if (!res.ok) {
		statusSet("Login failed: " + res.status);
		return;
	}

	const data = await res.json();
	localStorage.setItem("accessToken", data.accessToken);
	localStorage.setItem("refreshToken", data.refreshToken);
	statusSet("Logged in as user " + (data.user ? data.user.name : id));
}

async function refreshTokens() {
	const refreshToken = localStorage.getItem("refreshToken");
	if (!refreshToken) {
		statusSet("No refresh token");
		return false;
	}

	const res = await fetch("/api/v1/auth/refresh", {
		method: "POST",
		headers: { "Content-Type": "application/json", Authorization: "Bearer " + localStorage.getItem("accessToken") },
		body: JSON.stringify({ refreshToken }),
	});

	if (!res.ok) {
		statusSet("Refresh failed: " + res.status);
		return false;
	}

	const data = await res.json();
	localStorage.setItem("accessToken", data.accessToken);
	localStorage.setItem("refreshToken", data.refreshToken);
	statusSet("Tokens refreshed");
	return true;
}

async function logout() {
	const uid = document.getElementById("userid").value;
	const userId = "u" + uid;
	await fetch("/api/v1/auth/logout", {
		method: "POST",
		headers: { "Content-Type": "application/json", Authorization: "Bearer " + localStorage.getItem("accessToken") },
		body: JSON.stringify({ userId }),
	});
	localStorage.removeItem("accessToken");
	localStorage.removeItem("refreshToken");
	statusSet("Logged out");
}

// authFetch adds Authorization header and will attempt one refresh+retry on 401
async function authFetch(url, opts = {}) {
	const token = localStorage.getItem("accessToken");
	opts.headers = opts.headers || {};
	if (token) opts.headers["Authorization"] = "Bearer " + token;

	let res = await fetch(url, opts);
	if (res.status === 401) {
		const ok = await refreshTokens();
		if (!ok) return res;
		const newToken = localStorage.getItem("accessToken");
		opts.headers["Authorization"] = "Bearer " + newToken;
		res = await fetch(url, opts);
	}

	return res;
}

async function callProtected() {
	const res = await authFetch("/api/v1/protected/sample");
	if (!res.ok) {
		statusSet("Protected call failed: " + res.status);
		return;
	}
	const data = await res.json();
	statusSet(JSON.stringify(data, null, 2));
}

window.onload = () => {
	document.getElementById("btnLogin").onclick = login;
	document.getElementById("btnProtected").onclick = callProtected;
	document.getElementById("btnRefresh").onclick = refreshTokens;
	document.getElementById("btnLogout").onclick = logout;
	const at = localStorage.getItem("accessToken");
	if (at) statusSet("Already have token");
};
