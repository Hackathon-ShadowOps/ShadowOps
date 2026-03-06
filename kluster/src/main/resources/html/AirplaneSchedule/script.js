(() => {
	const timelineEl = document.getElementById("timeline");
	const trackEl = document.getElementById("track");
	const rulerEl = document.getElementById("ruler");
	const refreshBtn = document.getElementById("refresh");
	const spanSelect = document.getElementById("spanSelect");
	const statusIndicator = document.getElementById("status-indicator");

	const scheduleForm = document.getElementById("create-schedule-form");
	const scheduleFeedback = document.getElementById("schedule-feedback");
	const scheduleList = document.getElementById("schedule-list");

	const TIMELINE_COUNT = 10;
	const LANE_HEIGHT = 34;
	const LANE_GAP = 6;

	let viewSpanMinutes = parseInt(spanSelect.value, 10);
	let timelineStart = Math.floor(Date.now() / 60000) - 30;
	let timelineEnd = timelineStart + viewSpanMinutes;
	let wheelScrollAccum = 0;
	let wheelScrollTimer = null;
	let loadToken = 0;
	let statusTimer = null;
	let allSchedules = [];

	function formatHM(mins) {
		const d = new Date(mins * 60000);
		return d.toISOString().substr(11, 5);
	}

	function formatDateTime(epochMinutes) {
		return new Date(epochMinutes * 60000).toLocaleString();
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

	function checkOverlap(scheduleId, groundSpace, startTime, endTime) {
		for (const s of allSchedules) {
			if (scheduleId != null && Number(s.databaseId) === Number(scheduleId)) {
				continue;
			}
			if (Number(s.groundSpace) !== Number(groundSpace)) {
				continue;
			}
			if (startTime < Number(s.landingTimeEnd) && endTime > Number(s.landingTimeStart)) {
				return true;
			}
		}
		return false;
	}

	function renderSchedules(schedules) {
		allSchedules = schedules;
		clearBlocks();
		buildTrackLanes();

		for (const s of schedules) {
			const start = Number(s.landingTimeStart);
			const end = Number(s.landingTimeEnd);
			const block = document.createElement("div");
			block.className = "schedule-block";
			block.dataset.scheduleId = String(s.databaseId);
			block.dataset.airplaneId = String(s.airplaneId);
			block.dataset.groundSpace = String(s.groundSpace);
			block.dataset.start = String(start);
			block.dataset.end = String(end);

			renderBlockPosition(block, start, end, s.groundSpace);

			const leftHandle = document.createElement("div");
			leftHandle.className = "handle left";

			const label = document.createElement("div");
			label.className = "label";
			label.textContent = s.airplaneId + " (" + formatHM(start) + " - " + formatHM(end) + ") G" + s.groundSpace;

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
		let start0 = 0;
		let end0 = 0;
		let x0 = 0;
		let ground0 = 1;

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

			let newStart = start0;
			let newEnd = end0;
			let newGround = ground0;

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

			block.dataset.start = String(newStart);
			block.dataset.end = String(newEnd);
			block.dataset.groundSpace = String(newGround);
			renderBlockPosition(block, newStart, newEnd, newGround);
			block.querySelector(".label").textContent = block.dataset.airplaneId + " (" + formatHM(newStart) + " - " + formatHM(newEnd) + ") G" + newGround;
		}

		async function onUp() {
			if (!mode) return;
			document.body.style.userSelect = "";
			block.classList.remove("dragging");

			const nextScheduleId = Number(block.dataset.scheduleId);
			const nextGround = Number(block.dataset.groundSpace);
			const nextStart = Number(block.dataset.start);
			const nextEnd = Number(block.dataset.end);

			const changed = start0 !== nextStart || end0 !== nextEnd;
			const laneChanged = ground0 !== nextGround;

			if (changed || laneChanged) {
				if (checkOverlap(nextScheduleId, nextGround, nextStart, nextEnd)) {
					showStatus("Cannot overlap schedules", false);
					block.dataset.start = String(start0);
					block.dataset.end = String(end0);
					block.dataset.groundSpace = String(ground0);
					renderBlockPosition(block, start0, end0, ground0);
					block.querySelector(".label").textContent = block.dataset.airplaneId + " (" + formatHM(start0) + " - " + formatHM(end0) + ") G" + ground0;
					mode = null;
					return;
				}

				const ok = await updateSchedule(nextScheduleId, nextGround, nextStart, nextEnd);
				if (!ok) {
					showStatus("Failed to save", false);
					block.dataset.start = String(start0);
					block.dataset.end = String(end0);
					block.dataset.groundSpace = String(ground0);
					renderBlockPosition(block, start0, end0, ground0);
					block.querySelector(".label").textContent = block.dataset.airplaneId + " (" + formatHM(start0) + " - " + formatHM(end0) + ") G" + ground0;
				} else {
					showStatus("Saved", true);
					await loadSchedules();
					await getSchedules();
				}
			}
			mode = null;
		}
	}

	async function updateSchedule(scheduleId, groundSpace, start, end) {
		try {
			const params = new URLSearchParams({
				scheduleId: String(scheduleId),
				groundSpace: String(groundSpace),
				startTime: String(Math.floor(start)),
				endTime: String(Math.floor(end)),
			});
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
		clearBlocks();
		buildTrackLanes();
		const token = ++loadToken;

		const params = new URLSearchParams({
			startTime: String(timelineStart),
			endTime: String(timelineEnd),
		});
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
			schedules.forEach((schedule) => {
				const li = document.createElement("li");
				const info = document.createElement("span");
				info.textContent = `${schedule.airplaneId} | Ground Space: ${schedule.groundSpace} | ${formatDateTime(schedule.landingTimeStart)} - ${formatDateTime(schedule.landingTimeEnd)}`;

				const removeBtn = document.createElement("button");
				removeBtn.textContent = "Remove";
				removeBtn.onclick = async () => {
					if (confirm(`Remove schedule for ${schedule.airplaneId}?`)) {
						await removeSchedule(schedule.airplaneId);
					}
				};

				li.appendChild(info);
				li.appendChild(removeBtn);
				scheduleList.appendChild(li);
			});
		} catch (err) {
			scheduleFeedback.textContent = `Failed to get schedule: ${err.message}`;
		}
	}

	async function removeSchedule(airplaneId) {
		try {
			const res = await fetch(`/api/v1/removeAirplaneSchedule?airplaneId=${encodeURIComponent(airplaneId)}`, {
				method: "DELETE",
			});

			if (!res.ok) {
				const errText = await res.text();
				scheduleFeedback.textContent = `Failed to remove schedule: ${errText}`;
				return;
			}

			scheduleFeedback.textContent = "Schedule removed successfully.";
			await getSchedules();
			await loadSchedules();
		} catch (err) {
			scheduleFeedback.textContent = `Failed to remove schedule: ${err.message}`;
		}
	}

	scheduleForm?.addEventListener("submit", async (e) => {
		e.preventDefault();

		const startTimeInput = document.getElementById("schedule-start").value.trim();
		const endTimeInput = document.getElementById("schedule-end").value.trim();
		const airplaneId = document.getElementById("schedule-name").value.trim();
		const groundSpace = parseInt(document.getElementById("schedule-ground-space").value, 10);

		if (!startTimeInput || !endTimeInput || !airplaneId || !groundSpace) {
			scheduleFeedback.textContent = "Please fill in all required fields.";
			return;
		}

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

		const startTime = Math.floor(startTimeMs / 60000);
		const endTime = Math.floor(endTimeMs / 60000);

		if (checkOverlap(null, groundSpace, startTime, endTime)) {
			scheduleFeedback.textContent = `Schedule overlaps with an existing schedule on ground space ${groundSpace}.`;
			return;
		}

		scheduleFeedback.textContent = "Creating schedule...";

		try {
			const payload = {
				airplaneId,
				startTime,
				endTime,
				groundSpace,
			};

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

			await res.text();
			scheduleForm.reset();
			scheduleFeedback.textContent = "Schedule created successfully.";
			await getSchedules();
			await loadSchedules();
		} catch (err) {
			scheduleFeedback.textContent = `Failed to create schedule: ${err.message}`;
		}
	});

	refreshBtn.addEventListener("click", async () => {
		await loadSchedules();
		await getSchedules();
	});

	spanSelect.addEventListener("change", () => {
		viewSpanMinutes = parseInt(spanSelect.value, 10);
		loadSchedules();
	});

	timelineEl.addEventListener("wheel", onTimelineWheel, { passive: false });
	window.addEventListener("resize", buildRuler);

	buildRuler();
	loadSchedules(true);
	getSchedules();
})();
