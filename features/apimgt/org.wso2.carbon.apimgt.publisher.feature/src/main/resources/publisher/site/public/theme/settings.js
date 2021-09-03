const Settings = {
    app: {
        context: '/publisher',
        customUrl: {
            enabled: false,
            forwardedHeader: 'X-Forwarded-For',
        },
        origin: {
            host: 'localhost',
        },
        markdown: {
            skipHtml: true,
        },
        supportedDocTypes: 'application/msword, application/vnd.openxmlformats-officedocument.wordprocessingml.document,'
        + ' application/pdf, text/plain, application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,'
        + ' application/vnd.oasis.opendocument.text, application/vnd.oasis.opendocument.spreadsheet,'
        + ' application/json, application/x-yaml, .md',
    },
};
