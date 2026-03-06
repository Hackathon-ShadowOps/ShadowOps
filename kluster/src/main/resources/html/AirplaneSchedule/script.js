(() => {
	const timelineEl = document.getElementById("timeline");
	const trackEl = document.getElementById("track");
	const rulerEl = document.getElementById("ruler");
	const refreshBtn = document.getElementById("refresh");
	const spanSelect = document.getElementById("spanSelect");
	const statusIndicator = document.getElementById("status-indicator");
	const TIMELINE_COUNT = 10;
	const LANE_HEIGHT = 34;
	const LANE_GAP = 6;

	let viewSpanMinutes = parseInt(spanSelect.value, 10); // minutes
	let timelineStart = Math.floor(Date.now() / 60000) - 30; // minute epoch
	let timelineEnd = timelineStart + viewSpanMinutes;
	let wheelScrollAccum = 0;
	let wheelScrollTimer = null;
	let loadToken = 0;
	let statusTimer = null;

	function formatHM(mins) {
		const d = new Date(mins * 60000);
		return d.toISOString().substr(11, 5);
	}

	function showStatus(message, isSuccess) {
		if (statusTimer) {
			clearTimeout(statusTimer);
		}
		statusIndicator.textContent = message;
		statusIndicator.className = "status-indicator " + (isSuccess ? "success" : "error");
		statusTimer = setTimeout(() => {
			statusIndicator.className = "status-indicator";
			statusTimer = null;
		}, 2000);
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

	function laneTop(groundSpace) {
		const lane = Math.max(1, Math.min(TIMELINE_COUNT, Number(groundSpace) || 1));
		return (lane - 1) * (LANE_HEIGHT + LANE_GAP) + 3;
	}

	function yToLane(y, rectTop) {
		const laneSize = LANE_HEIGHT + LANE_GAP;
		const lane = Math.floor((y - rectTop) / laneSize) + 1;
		return Math.max(1, Math.min(TIMELINE_COUNT, lane));
	}

	function buildTrackLanes() {
		const totalHeight = TIMELINE_COUNT * (LANE_HEIGHT + LANE_GAP);
		trackEl.style.height = totalHeight + "px";
		for (let lane = 1; lane <= TIMELINE_COUNT; lane += 1) {
			const laneEl = document.createElement("div");
			laneEl.className = "timeline-lane";
			laneEl.style.top = (lane - 1) * (LANE_HEIGHT + LANE_GAP) + "px";
			laneEl.style.height = LANE_HEIGHT + "px";
			laneEl.textContent = "Lane " + lane;
			trackEl.appendChild(laneEl);
		}
	}

	function minuteToPx(min) {
		const rect = trackEl.getBoundingClientRect();
		const span = timelineEnd - timelineStart;
		return ((min - timelineStart) / span) * rect.width;
	}

	function renderBlockPosition(block, start, end, groundSpace) {
		const visibleStart = Math.max(start, timelineStart);
		const visibleEnd = Math.min(end, timelineEnd);
		if (visibleEnd <= visibleStart) {
			block.style.display = "none";
			return;
		}
		block.style.display = "flex";
		const left = minuteToPx(visibleStart);
		const right = minuteToPx(visibleEnd);
		block.style.left = left + "px";
		block.style.width = Math.max(10, right - left) + "px";
		block.style.top = laneTop(groundSpace) + "px";
	}

	function renderSchedules(schedules) {
		clearBlocks();
		buildTrackLanes();
		for (const s of schedules) {
			const start = s.landingTimeStart;
			const end = s.landingTimeEnd;
			const block = document.createElement("div");
			block.className = "schedule-block";
			block.dataset.scheduleId = s.databaseId;
			block.dataset.airplaneId = s.airplaneId;
			block.dataset.groundSpace = s.groundSpace;
			block.dataset.start = start;
			block.dataset.end = end;

			renderBlockPosition(block, start, end, s.groundSpace);

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
			x0 = 0,
			ground0 = 1;

		leftHandle.addEventListener("mousedown", (ev) => {
			ev.stopPropagation();
			mode = "resize-left";
			start0 = Number(block.dataset.start);
			end0 = Number(block.dataset.end);
			ground0 = Number(block.dataset.groundSpace) || 1;
			x0 = ev.clientX;
			document.body.style.userSelect = "none";
		});
		rightHandle.addEventListener("mousedown", (ev) => {
			ev.stopPropagation();
			mode = "resize-right";
			start0 = Number(block.dataset.start);
			end0 = Number(block.dataset.end);
			ground0 = Number(block.dataset.groundSpace) || 1;
			x0 = ev.clientX;
			document.body.style.userSelect = "none";
		});

		block.addEventListener("mousedown", (ev) => {
			mode = "drag";
			start0 = Number(block.dataset.start);
			end0 = Number(block.dataset.end);
			ground0 = Number(block.dataset.groundSpace) || 1;
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
				newEnd = end0,
				newGround = ground0;
			if (mode === "drag") {
				newStart = start0 + minutesDelta;
				newEnd = end0 + minutesDelta;
				newGround = yToLane(ev.clientY, rect.top);
			}
			if (mode === "resize-left") {
				newStart = start0 + minutesDelta;
				if (newStart >= end0 - 5) newStart = end0 - 5;
			}
			if (mode === "resize-right") {
				newEnd = end0 + minutesDelta;
				if (newEnd <= start0 + 5) newEnd = start0 + 5;
			}
			block.dataset.start = newStart;
			block.dataset.end = newEnd;
			block.dataset.groundSpace = newGround;
			renderBlockPosition(block, newStart, newEnd, newGround);
			block.querySelector(".label").textContent = block.dataset.airplaneId + " (" + formatHM(newStart) + " - " + formatHM(newEnd) + ") G" + newGround;
		}

		async function onUp(ev) {
			if (!mode) return;
			document.body.style.userSelect = "";
			block.classList.remove("dragging");
			const changed = start0 !== Number(block.dataset.start) || end0 !== Number(block.dataset.end);
			const laneChanged = ground0 !== Number(block.dataset.groundSpace);
			if (changed || laneChanged) {
				const ok = await updateSchedule(Number(block.dataset.scheduleId), Number(block.dataset.groundSpace), Number(block.dataset.start), Number(block.dataset.end));
				if (!ok) {
					showStatus("Failed to save", false);
					block.dataset.start = start0;
					block.dataset.end = end0;
					block.dataset.groundSpace = ground0;
					renderBlockPosition(block, start0, end0, ground0);
					block.querySelector(".label").textContent = block.dataset.airplaneId + " (" + formatHM(start0) + " - " + formatHM(end0) + ") G" + ground0;
				} else {
					showStatus("Saved", true);
					await loadSchedules();
				}
			}
			mode = null;
		}
	}

	async function updateSchedule(scheduleId, groundSpace, start, end) {
		try {
			const params = new URLSearchParams({ scheduleId: String(scheduleId), groundSpace: String(groundSpace), startTime: String(Math.floor(start)), endTime: String(Math.floor(end)) });
			const res = await fetch("/api/v1/updateAirplaneSchedule?" + params.toString(), { method: "POST" });
			if (!res.ok) {
				console.warn("Update failed", await res.text());
				return false;
			}
			return true;
		} catch (e) {
			console.error(e);
			return false;
		}
	}

	function applyTimelineShift(minutes) {
		timelineStart += minutes;
		loadSchedules();
	}

	function onTimelineWheel(ev) {
		ev.preventDefault();
		const baseStep = Math.max(1, Math.round(viewSpanMinutes / 24));
		const direction = ev.deltaY > 0 ? 1 : -1;
		const multiplier = Math.max(1, Math.min(8, Math.ceil(Math.abs(ev.deltaY) / 80)));
		wheelScrollAccum += direction * baseStep * multiplier;
		if (wheelScrollTimer) {
			clearTimeout(wheelScrollTimer);
		}
		wheelScrollTimer = setTimeout(() => {
			if (wheelScrollAccum !== 0) {
				applyTimelineShift(wheelScrollAccum);
				wheelScrollAccum = 0;
			}
			wheelScrollTimer = null;
		}, 50);
	}

	async function loadSchedules(resetToNow = false) {
		if (resetToNow) {
			timelineStart = Math.floor(Date.now() / 60000) - 30;
		}
		timelineEnd = timelineStart + viewSpanMinutes;
		buildRuler();
		const token = ++loadToken;
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
		if (token !== loadToken) {
			return;
		}
		renderSchedules(schedules);
	}

	refreshBtn.addEventListener("click", () => loadSchedules());
	spanSelect.addEventListener("change", () => {
		viewSpanMinutes = parseInt(spanSelect.value, 10);
		loadSchedules();
	});
	timelineEl.addEventListener("wheel", onTimelineWheel, { passive: false });

	window.addEventListener("resize", buildRuler);
	// initial load
	buildRuler();
	loadSchedules(true);
})();

async function getSchedules() {
	try {
		const res = await fetch("/api/v1/airplaneLandingSchedule", {
			method: "GET",
			headers: { "Content-Type": "application/json" },
		});

		if (!res.ok) {
			const errText = await res.text();
			scheduleFeedback.textContent = `Failed to get schedule: ${errText}`;
			return;
		}

		const text = await res.text();
		let schedules = [];
		try {
			schedules = JSON.parse(text);
		} catch (e) {
			scheduleFeedback.textContent = "Failed to parse schedule data.";
			return;
		}

		scheduleList.innerHTML = "";
		schedules.forEach(renderScheduleItem);
	} catch (err) {
		scheduleFeedback.textContent = `Failed to get schedule: ${err.message}`;
	}
}

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
		endTime,
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
