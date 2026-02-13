function getLanding() {
	fetch("/")
		.then((response) => response.json())
		.then((data) => {
			console.log("Maps data:", data);
			// Hantera den hämta datan här
		})
		.catch((error) => console.error("Error fetching maps:", error));
}

window.onload = () => {
	getLanding();
};
