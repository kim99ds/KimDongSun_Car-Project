    (function () {
    const qText = document.getElementById("qText");
    const list = document.getElementById("trimCardList");
    if (!qText || !list) return;

    qText.addEventListener("input", () => {
        const q = (qText.value || "").trim().toLowerCase();
        list.querySelectorAll("li.car-item").forEach((item) => {
            const name = (item.getAttribute("data-name") || "").toLowerCase();
            item.style.display = (!q || name.includes(q)) ? "" : "none";
        });
    });
})();
