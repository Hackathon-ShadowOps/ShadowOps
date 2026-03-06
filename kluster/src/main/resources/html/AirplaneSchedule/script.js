(() => {
	const timelineEl = document.getElementById("timeline");
	const trackEl = document.getElementById("track");
	const rulerEl = document.getElementById("ruler");
	const refreshBtn = document.getElementById("refresh");
	const spanSelect = document.getElementById("spanSelect");

	let viewSpanMinutes = parseInt(spanSelect.value, 10); // minutes
	let timelineStart = Math.floor(Date.now() / 60000) - 30; // minute epoch
	let timelineEnd = timelineStart + viewSpanMinutes;

	function formatHM(mins) {
		const d = new Date(mins * 60000);
		return d.toISOString().substr(11, 5);
	}

	function buildRuler() {
		rulerEl.innerHTML = "";
		const width = timelineEl.clientWidth;
		const span = timelineEnd - timelineStart;
		const major = Math.max(5, Math.floor(span / 6));
		for (let t = timelineStart; t <= timelineEnd; t += major) {
			const pct = ((t - timelineStart) / span) * 100;
			const tick = document.createElement("div");
			tick.className = "tick";
			tick.style.left = pct + "%";
			const label = document.createElement("span");
			label.textContent = formatHM(t);
			tick.appendChild(label);
			rulerEl.appendChild(tick);
		}
	}

	function clearBlocks() {
		trackEl.innerHTML = "";
	}

	function minuteToPx(min) {
		const rect = trackEl.getBoundingClientRect();
		const span = timelineEnd - timelineStart;
		return ((min - timelineStart) / span) * rect.width;
	}

	function renderSchedules(schedules) {
		clearBlocks();
		for (const s of schedules) {
			const start = s.landingTimeStart;
			const end = s.landingTimeEnd;
			const block = document.createElement("div");
			block.className = "schedule-block";
			block.dataset.airplaneId = s.airplaneId;
			block.dataset.groundSpace = s.groundSpace;
			block.dataset.start = start;
			block.dataset.end = end;

			const left = minuteToPx(Math.max(start, timelineStart));
			const right = minuteToPx(Math.min(end, timelineEnd));
			const width = Math.max(10, right - left);
			block.style.left = left + "px";
			block.style.width = width + "px";

			const leftHandle = document.createElement("div");
			leftHandle.className = "handle left";
			const label = document.createElement("div");
			label.className = "label";
			label.textContent = s.airplaneId + " (" + formatHM(start) + " - " + formatHM(end) + ")";
			const rightHandle = document.createElement("div");
			rightHandle.className = "handle right";
			block.appendChild(leftHandle);
			block.appendChild(label);
			block.appendChild(rightHandle);

			attachInteractions(block, leftHandle, rightHandle);
			trackEl.appendChild(block);
		}
	}

	function attachInteractions(block, leftHandle, rightHandle) {
		let mode = null;
		let start0 = 0,
			end0 = 0,
			x0 = 0;

		leftHandle.addEventListener("mousedown", (ev) => {
			ev.stopPropagation();
			mode = "resize-left";
			start0 = Number(block.dataset.start);
			end0 = Number(block.dataset.end);
			x0 = ev.clientX;
			document.body.style.userSelect = "none";
		});
		rightHandle.addEventListener("mousedown", (ev) => {
			ev.stopPropagation();
			mode = "resize-right";
			start0 = Number(block.dataset.start);
			end0 = Number(block.dataset.end);
			x0 = ev.clientX;
			document.body.style.userSelect = "none";
		});

		block.addEventListener("mousedown", (ev) => {
			mode = "drag";
			start0 = Number(block.dataset.start);
			end0 = Number(block.dataset.end);
			x0 = ev.clientX;
			block.classList.add("dragging");
			document.body.style.userSelect = "none";
		});

		document.addEventListener("mousemove", onMove);
		document.addEventListener("mouseup", onUp);

		function onMove(ev) {
			if (!mode) return;
			const rect = trackEl.getBoundingClientRect();
			const dx = ev.clientX - x0;
			const span = timelineEnd - timelineStart;
			const minutesDelta = Math.round((dx / rect.width) * span);
			let newStart = start0,
				newEnd = end0;
			if (mode === "drag") {
				newStart = start0 + minutesDelta;
				newEnd = end0 + minutesDelta;
			}
			if (mode === "resize-left") {
				newStart = start0 + minutesDelta;
				if (newStart >= end0 - 5) newStart = end0 - 5;
			}
			if (mode === "resize-right") {
				newEnd = end0 + minutesDelta;
				if (newEnd <= start0 + 5) newEnd = start0 + 5;
			}
			// clamp to timeline
			if (newStart < timelineStart) {
				const diff = timelineStart - newStart;
				newStart = timelineStart;
				newEnd += diff;
			}
			if (newEnd > timelineEnd) {
				const diff = newEnd - timelineEnd;
				newEnd = timelineEnd;
				newStart -= diff;
			}
			block.dataset.start = newStart;
			block.dataset.end = newEnd;
			const leftPx = minuteToPx(newStart);
			const rightPx = minuteToPx(newEnd);
			block.style.left = leftPx + "px";
			block.style.width = Math.max(10, rightPx - leftPx) + "px";
			block.querySelector(".label").textContent = block.dataset.airplaneId + " (" + formatHM(newStart) + " - " + formatHM(newEnd) + ")";
		}

		function onUp(ev) {
			if (!mode) return;
			document.body.style.userSelect = "";
			block.classList.remove("dragging");
			const changed = start0 !== Number(block.dataset.start) || end0 !== Number(block.dataset.end);
			if (changed) {
				// send update to backend
				updateSchedule(block.dataset.airplaneId, Number(block.dataset.groundSpace), Number(block.dataset.start), Number(block.dataset.end));
			}
			mode = null;
		}
	}

	async function updateSchedule(airplaneId, groundSpace, start, end) {
		try {
			const params = new URLSearchParams({ airplaneId, groundSpace: String(groundSpace), startTime: String(Math.floor(start)), endTime: String(Math.floor(end)) });
			const res = await fetch("/api/v1/updateAirplaneSchedule?" + params.toString(), { method: "POST" });
			if (!res.ok) console.warn("Update failed", await res.text());
		} catch (e) {
			console.error(e);
		}
	}

	async function loadSchedules() {
		timelineStart = Math.floor(Date.now() / 60000) - 30;
		timelineEnd = timelineStart + viewSpanMinutes;
		buildRuler();
		const params = new URLSearchParams({ startTime: String(timelineStart), endTime: String(timelineEnd) });
		const res = await fetch("/api/v1/airplaneLandingSchedule?" + params.toString());
		if (!res.ok) {
			console.error("Failed to load schedules");
			return;
		}
		const text = await res.text();
		let schedules = [];
		try {
			schedules = JSON.parse(text);
		} catch (e) {
			console.error("Invalid JSON", text);
			return;
		}
		renderSchedules(schedules);
	}

	refreshBtn.addEventListener("click", () => loadSchedules());
	spanSelect.addEventListener("change", () => {
		viewSpanMinutes = parseInt(spanSelect.value, 10);
		loadSchedules();
	});

	window.addEventListener("resize", buildRuler);
	// initial load
	buildRuler();
	loadSchedules();
})();

const scheduleForm = document.getElementById("create-schedule-form");
const scheduleFeedback = document.getElementById("schedule-feedback");
const scheduleList = document.getElementById("schedule-list");

function renderScheduleItem(schedule) {
	const li = document.createElement("li");
	li.textContent = `${schedule.name} | ${schedule.cron} | ${schedule.timezone} | ${schedule.enabled ? "enabled" : "disabled"}`;
	scheduleList.prepend(li);
}

scheduleForm?.addEventListener("submit", async (e) => {
	e.preventDefault();

	const startTimeInput = document.getElementById("schedule-start").value.trim();
	const endTimeInput = document.getElementById("schedule-end").value.trim();

	if (!startTimeInput || !endTimeInput) {
		scheduleFeedback.textContent = "Please provide both start and end times.";
		return;
	}

	// Date.parse gives epoch milliseconds
	const startTimeMs = Date.parse(startTimeInput);
	const endTimeMs = Date.parse(endTimeInput);

	if (isNaN(startTimeMs) || isNaN(endTimeMs)) {
		scheduleFeedback.textContent = "Invalid date format. Please use the date picker.";
		return;
	}

	if (endTimeMs <= startTimeMs) {
		scheduleFeedback.textContent = "End time must be after start time.";
		return;
	}

	// Normalize to epoch minutes to match the rest of this page/API usage
	const startTime = Math.floor(startTimeMs / 60000);
	const endTime = Math.floor(endTimeMs / 60000);

	const payload = {
		airplaneId: document.getElementById("schedule-name").value.trim(),
		startTime,
		endTime
	};

	if (!payload.airplaneId || !payload.startTime || !payload.endTime) {
		scheduleFeedback.textContent = "Please fill in all required fields.";
		return;
	}

	scheduleFeedback.textContent = "Creating schedule...";

	try {
		const res = await fetch("/api/v1/addAirplaneSchedule", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(payload),
		});

		if (!res.ok) {
			const errText = await res.text();
			scheduleFeedback.textContent = `Failed to create schedule: ${errText}`;
			return;
		}

		// Backend currently returns plain text, not JSON
		await res.text();

		scheduleForm.reset();
		document.getElementById("schedule-enabled").checked = true;
		document.getElementById("schedule-timezone").value = "UTC";
		scheduleFeedback.textContent = "Schedule created successfully.";

		// Refresh timeline after create
		loadSchedules();
	} catch (err) {
		scheduleFeedback.textContent = `Failed to create schedule: ${err.message}`;
	}
});
