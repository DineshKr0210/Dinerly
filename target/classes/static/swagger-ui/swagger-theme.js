const THEME_KEY = "dinerly-swagger-theme";

function applyTheme(theme) {
    document.body.classList.toggle("light-mode", theme === "light");

    const toggle = document.getElementById("themeToggle");
    if (toggle) {
        toggle.textContent = theme === "light"
            ? "Dark Mode"
            : "Light Mode";
    }

    localStorage.setItem(THEME_KEY, theme);
}

function initTheme() {
    const savedTheme = localStorage.getItem(THEME_KEY) || "dark";

    applyTheme(savedTheme);

    const toggle = document.getElementById("themeToggle");

    if (toggle) {
        toggle.addEventListener("click", () => {
            const nextTheme =
                document.body.classList.contains("light-mode")
                    ? "dark"
                    : "light";

            applyTheme(nextTheme);
        });
    }
}

function buildControllerNavigation(openApiDoc) {
    const nav = document.getElementById("controllerNav");

    if (!nav || !openApiDoc || !openApiDoc.tags) {
        return;
    }

    const uniqueTags = [
        ...new Map(openApiDoc.tags.map(tag => [tag.name, tag])).values()
    ];

    nav.innerHTML = "";

    uniqueTags.forEach(tag => {

        const link = document.createElement("a");

        link.className = "controller-link";

        link.href = `#/${tag.name}`;

        link.textContent = tag.name;

        link.addEventListener("click", () => {

            nav.querySelectorAll(".controller-link")
                .forEach(item => item.classList.remove("active"));

            link.classList.add("active");
        });

        nav.appendChild(link);
    });
}

function loadControllerNavigation() {

    fetch("/v3/api-docs")
        .then(response => response.json())
        .then(doc => buildControllerNavigation(doc))
        .catch(console.error);

}

function initSwaggerUI() {

    window.ui = SwaggerUIBundle({

        url: "/v3/api-docs",

        dom_id: "#swagger-ui-root",

        deepLinking: true,

        persistAuthorization: true,

        displayRequestDuration: true,

        filter: true,

        operationsSorter: "alpha",

        tagsSorter: "alpha",

        tryItOutEnabled: true,

        defaultModelsExpandDepth: 2,

        defaultModelExpandDepth: 2,

        docExpansion: "none",

        supportedSubmitMethods: [
            "get",
            "post",
            "put",
            "patch",
            "delete"
        ],

        onComplete: () => {

            loadControllerNavigation();

            console.log("Swagger UI Loaded Successfully");

        }

    });

}

window.addEventListener("DOMContentLoaded", () => {

    initTheme();

    initSwaggerUI();

});