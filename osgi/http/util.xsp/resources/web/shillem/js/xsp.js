function XPages() {
    const _self = this;

    const _SubmitListener = function (formId, listener, clientId, scriptId) {
        this.formId = formId;
        this.clientId = clientId;
        this.scriptId = scriptId;
        this.listener = listener;

        this.run = function () {
            return this.listener();
        };
    };

    const _attachEventOrPartial = function (params) {
        if (!params.eventName) {
            return;
        }

        const element = document.getElementById(
            params.targetId ? params.targetId : params.clientId
        );

        if (!element && params.nonPartialEvent) {
            return;
        }

        const elements = [];

        if (element && element.nodeName === "FIELDSET" && params.eventName === "onclick") {
            document.body.querySelectorAll("input").forEach((input) => {
                if (input.name === element.id) {
                    const type = input.type ? input.type.toLowerCase() : "text";

                    if (type === "checkbox" || type === "radio") {
                        elements.push(input);
                    }
                }
            });
        }

        if (elements.length === 0) {
            elements.push(element);
        }

        elements.forEach((el) =>
            el.addEventListener(params.eventName.replace(/^on/, ""), function (evt) {
                if (element && element.getAttribute) {
                    const href = element.getAttribute("href");

                    if (href && href.endsWith("#")) {
                        evt.preventDefault();
                    }
                }

                const form = _self.findForm(element.id);

                if (params.partialEvent) {
                    if (!form || !_self.canSubmit()) {
                        return;
                    }

                    const fired = _fireEvent(
                        evt,
                        form,
                        params.clientId,
                        params.scriptName,
                        true,
                        params.valmode,
                        params.execId
                    );

                    if (fired) {
                        _partialRefresh("post", form, params.partialRefreshId, {
                            onComplete: params.partialOnComplete,
                            onError: params.partialOnError,
                            onStart: params.partialOnStart,
                            targetId: element.id,
                        });
                    } else {
                        _self.allowSubmit();
                    }
                } else {
                    if (params.nonPartialSubmit && (!form || !_self.canSubmit())) {
                        return;
                    }

                    const fired = _fireEvent(
                        evt,
                        form,
                        params.clientId,
                        params.scriptName,
                        params.nonPartialSubmit,
                        params.valmode,
                        params.execId
                    );

                    if (params.nonPartialSubmit) {
                        if (fired) {
                            window.onbeforeunload = null;

                            form.submit();
                        } else {
                            _self.allowSubmit();
                        }
                    }
                }
            })
        );
    };
    const _execScripts = function (scripts) {
        scripts.forEach((scp) => {
            const index = scp.indexOf("<!--");

            if (index > -1) {
                const val = this.trim(scp);

                if (val.length >= 4 && -1 != val.lastIndexOf("<!--", 4)) {
                    scp = scp.substring(0, index) + "//" + scp.substring(index + 4);
                }
            }

            eval(scp);
        });
    };
    const _fireEvent = function (evt, form, clientId, clientScript, submit, valmode, execId) {
        let submitValue;

        if (typeof clientScript === "function") {
            try {
                submitValue = clientScript(evt);

                if (typeof submitValue === "boolean" && !submitValue) {
                    return false;
                }

                if (typeof submitValue === "object") {
                    submitValue = JSON.stringify(submitValue);
                } else if (submitValue != undefined && typeof submitValue !== "string") {
                    submitValue = submitValue.toString();
                }
            } catch (err) {
                console.error(err);

                return false;
            }
        }

        if (!submit) {
            return false;
        }

        if (
            typeof valmode === "number" &&
            valmode > 0 &&
            !_self.validateAll(form.id, valmode, execId)
        ) {
            return false;
        }

        if (!_processListeners(_querySubmitListeners, form.id, clientId)) {
            return false;
        }

        _processListeners(_preSubmitListeners, form.id, clientId);

        if (!form["$$viewid"]) {
            console.error(
                "Problem submitting the page. The form does not contain an input named: $$viewid"
            );

            return false;
        }

        let input;

        input = form["$$xspsubmitid"];
        if (input) input.value = clientId;

        input = form["$$xspexecid"];
        if (input) input.value = execId || "";

        input = form["$$xspsubmitvalue"];
        if (input) input.value = submitValue != undefined ? submitValue : "";

        input = form["$$xspsubmitscroll"];
        if (input) input.value = _scrollPosition();

        return true;
    };
    const _loaded = function () {
        _onLoadListeners.forEach(function (fn) {
            fn();
        });

        _onLoadListeners = [];
    };
    const _partialRefresh = function (method, form, refreshId, options) {
        options.onStart &&
            (typeof options.onStart === "function"
                ? options.onStart(options)
                : eval(options.onStart));

        let xhrurl =
            form.action +
            (form.action.match(/\?/) ? "&" : "?") +
            "$$ajaxid=" +
            encodeURIComponent(refreshId);
        let xhropt = { method: method.toUpperCase() };

        if (xhropt.method === "GET") {
            if (options.params) {
                if (typeof options.params === "object") {
                    const query = Object.keys(options.params)
                        .map((k) => k + "=" + encodeURIComponent(options.params[k]))
                        .join("&");

                    if (query.length > 0) {
                        xhrurl += "&" + query;
                    }
                } else {
                    if (typeof options.params === "string") {
                        xhrurl += "&" + options.params;
                    }
                }
            }

            const vid = form["$$viewid"].value;

            if (vid) {
                xhrurl += "&" + "$$viewid=" + vid;
            }
        } else {
            xhropt.body = new FormData(form);

            if (options.params) {
                Object.keys(options.params).forEach((k) =>
                    xhropt.body.append(k, options.params[k])
                );
            }
        }

        _timeout(
            _self.submitLatency,
            fetch(xhrurl, xhropt)
                .then((response) =>
                    response.text().then((text) => {
                        return {
                            headers: response.headers,
                            text,
                            ok: response.ok,
                            status: response.status,
                            statusText: response.statusText,
                        };
                    })
                )
                .then((response) => {
                    if (!response.ok) {
                        const err = new Error(response.statusText);
                        err.response = response;
                        throw err;
                    }

                    const headers = response.headers;

                    if (headers.has("X-XspLocation")) {
                        window.location = headers.get("X-XspLocation");

                        return;
                    }

                    if (headers.has("X-XspRefreshId")) {
                        refreshId = headers.get("X-XspRefreshId");
                    }

                    if (refreshId !== "@none") {
                        _replaceNode(refreshId, response.text);
                    }

                    _self.allowSubmit();

                    options.onComplete &&
                        (typeof options.onComplete === "function"
                            ? options.onComplete(options, response)
                            : eval(options.onComplete));
                })
                .catch((error) => {
                    let errorDisplayed = false;

                    if (error.response && error.response.text) {
                        const start = error.response.text.search(/<(!doctype )*html|/i);
                        const end = start < 0 ? -1 : error.response.text.search(/<\/html>/i);

                        if (start === 0 && end > 0) {
                            const page = document.open("text/html", "replace");
                            page.write(error.response.text);
                            page.close();

                            return;
                        }

                        if (
                            refreshId !== "@none" &&
                            error.response.text.search(new RegExp('<.+id="' + refreshId, "i")) > -1
                        ) {
                            _replaceNode(refreshId, error.response.text);

                            errorDisplayed = true;
                        }
                    }

                    _self.allowSubmit();

                    if (options.onError) {
                        typeof options.onError === "function"
                            ? options.onError(options, error)
                            : eval(options.onError);

                        errorDisplayed = true;
                    }

                    if (errorDisplayed) {
                        return;
                    }

                    let message = "An error occurred while updating the page.";
                    if (error.message) message += "\n" + error.message;
                    console.error(message);
                })
        );
    };
    const _processListeners = function (listeners, formId, targetId) {
        for (let listener of listeners) {
            if (listener.formId !== formId) {
                continue;
            }

            if (!listener.clientId || listener.clientId === targetId) {
                if (!listener.run() && listeners == _querySubmitListeners) {
                    return false;
                }
            }
        }

        return true;
    };
    const _processScripts = function (text, extract) {
        const re = new RegExp("(<script[^>]*>[\\S\\s]*?</script>)", "img");

        if (extract) {
            const values = [];

            text.replace(re, function (all, part) {
                values.push(part.substring(part.indexOf(">") + 1, part.lastIndexOf("<")));
            });

            return values;
        }

        return text.replace(re, "");
    };
    const _pushListener = function (listeners, formId, listener, clientId, scriptId) {
        if (!scriptId) {
            this._unnamedSubmitListenerCount++;

            scriptId = "script" + _unnamedSubmitListenerCount.toString();
        } else {
            for (let l of listeners) {
                if (scriptId === l.scriptId) {
                    return;
                }
            }
        }

        listeners.push(new _SubmitListener(formId, listener, clientId, scriptId));
    };
    const _replaceNode = function (refreshId, text) {
        const element = document.getElementById(refreshId);

        if (!element) {
            console.error(`No element to refresh with id ${refreshId}`);

            return;
        }

        const extractBlock = (startMarker, endMarker) => {
            const startIndex = text.indexOf(startMarker);

            if (startIndex < 0) {
                return;
            }

            const endIndex = text.lastIndexOf(endMarker);

            if (endIndex < 0) {
                return;
            }

            let block = text.substring(startIndex + startMarker.length, endIndex);

            text = text.substring(0, startIndex) + text.substring(endIndex + endMarker.length);

            return block;
        };

        const headerScripts = extractBlock(
            "<!-- XSP_UPDATE_HEADER_START -->\n",
            "<!-- XSP_UPDATE_HEADER_END -->\n"
        );

        const updateScripts = extractBlock(
            "<!-- XSP_UPDATE_SCRIPT_START -->",
            "<!-- XSP_UPDATE_SCRIPT_END -->\n"
        );

        if (headerScripts) {
            _execScripts(_processScripts(headerScripts, true));
        }

        const html = _processScripts(text, false);

        if (html) {
            element.outerHTML = html;
        } else {
            element.parentNode.removeChild(element);
        }

        const blockScripts = _processScripts(text, true);

        if (blockScripts) {
            _execScripts(blockScripts);
        }

        if (updateScripts) {
            _execScripts(_processScripts(updateScripts, true));
        }

        _loaded();
    };
    const _scrollPosition = function () {
        const x = window.pageXOffset || document.documentElement.scrollLeft || 0;
        const y = window.pageYOffset || document.documentElement.scrollTop || 0;

        return x + "|" + y;
    };
    const _timeout = function (ms, promise) {
        return new Promise((resolve, reject) => {
            const timer = setTimeout(() => {
                reject(new Error("TIMEOUT"));
            }, ms);

            _preFetchListeners.forEach((l) => l());

            promise
                .then((value) => {
                    clearTimeout(timer);
                    resolve(value);
                })
                .catch((reason) => {
                    clearTimeout(timer);
                    reject(reason);
                })
                .then(() => _postFetchListeners.forEach((l) => l()));
        });
    };

    let _onLoadCalled;
    let _onLoadListeners = [];
    let _postFetchListeners = [];
    let _preFetchListeners = [];
    let _preSubmitListeners = [];
    let _querySubmitListeners = [];
    let _unnamedSubmitListenerCount;

    this.addOnLoad = function (fn) {
        _onLoadListeners.push(fn);

        if (!_onLoadCalled) {
            _onLoadCalled = true;

            window.addEventListener("DOMContentLoaded", _loaded);
        }
    };
    this.addPostFetchListener = function (fn) {
        _postFetchListeners.push(fn);
    };
    this.addPreFetchListener = function (fn) {
        _preFetchListeners.push(fn);
    };
    this.addPreSubmitListener = function (formId, listener, clientId, scriptId) {
        _pushListener(_preSubmitListeners, formId, listener, clientId, scriptId);
    };
    this.addQuerySubmitListener = function (formId, listener, clientId, scriptId) {
        _pushListener(_querySubmitListeners, formId, listener, clientId, scriptId);
    };
    this.allowSubmit = function () {
        this.lastSubmit = 0;
    };
    this.attachEvent = function (
        clientId,
        targetId,
        eventName,
        scriptName,
        nonPartialSubmit,
        valmode,
        execId
    ) {
        return _attachEventOrPartial({
            clientId,
            targetId,
            eventName,
            scriptName,
            valmode,
            execId,
            nonPartialEvent: true,
            nonPartialSubmit,
            partialEvent: false,
            partialRefreshId: null,
            partialOnStart: null,
            partialOnComplete: null,
            partialOnError: null,
        });
    };
    this.attachPartial = function (
        clientId,
        targetId,
        execId,
        eventName,
        scriptName,
        valmode,
        partialRefreshId,
        partialOnStart,
        partialOnComplete,
        partialOnError
    ) {
        return _attachEventOrPartial({
            clientId,
            targetId,
            eventName,
            scriptName,
            valmode,
            execId,
            nonPartialEvent: false,
            nonPartialSubmit: false,
            partialEvent: true,
            partialRefreshId,
            partialOnStart,
            partialOnComplete,
            partialOnError,
        });
    };
    this.canSubmit = function () {
        if (this.lastSubmit > 0) {
            const now = new Date().getTime();

            if (now < this.lastSubmit + this.submitLatency) {
                return false;
            }
        }

        this.lastSubmit = new Date().getTime();

        return true;
    };
    this.findForm = function (id) {
        return document.getElementById(id).closest("form");
    };
    this.getElementById = function (id) {
        return document.getElementById(id);
    };
    this.lastSubmit = 0;
    this.partialRefreshGet = function (refreshId, options) {
        const params = options || {};

        const form = this.findForm(params.formId || params.execId || refreshId);

        if (!form || !this.canSubmit()) {
            return false;
        }

        _partialRefresh("get", form, refreshId || "@none", {
            onStart: params.onStart,
            onComplete: params.onComplete,
            onError: params.onError,
            params: params.params,
            targetId: params.clientId,
        });
    };
    this.partialRefreshPost = function (refreshId, options) {
        const params = options || {};

        const form = this.findForm(params.formId || params.execId || refreshId);

        if (!form || !this.canSubmit()) {
            return false;
        }

        const fired = _fireEvent(
            null,
            form,
            params.clientId || refreshId,
            null,
            true,
            params.immediate ? 0 : params.valmode,
            params.execId
        );

        if (fired) {
            _partialRefresh("post", form, refreshId || "@none", {
                onStart: params.onStart,
                onComplete: params.onComplete,
                onError: params.onError,
                params: params.params,
                targetId: params.clientId,
            });
        } else {
            this.allowSubmit();
        }
    };
    this.submitLatency = 20 * 1000;
    this.validateAll = function (formId, valmode, execId) {
        // Client validation dropped
        return true;
    };
}

// eslint-disable-next-line no-unused-vars
var XSP = new XPages();
