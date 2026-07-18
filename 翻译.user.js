// ==UserScript==
// @name         智能网页自动翻译（极速·迷你版）
// @namespace    http://tampermonkey.net/
// @version      3.5
// @description  自动翻译网页内容，支持并发、缓存、动态监听，悬浮窗可拖动贴边自动隐藏
// @author       Assistant
// @match        *://*/*
// @grant        GM_xmlhttpRequest
// @grant        GM_setValue
// @grant        GM_getValue
// @grant        GM_addStyle
// @connect      translate.googleapis.com
// @connect      translate.google.com
// @license      MIT
// ==/UserScript==

(function () {
    'use strict';

    // ======================== 配置（可调优） ========================
    const CONFIG = {
        defaultLang: 'zh-CN',
        maxTextLength: 1200,
        maxConcurrent: 8,
        batchDelay: 100,
        excludeTags: ['SCRIPT', 'STYLE', 'NOSCRIPT', 'TEXTAREA', 'INPUT', 'SELECT', 'BUTTON', 'CODE', 'PRE', 'SVG'],
        excludeSelectors: ['.notranslate', '[translate="no"]', '.no-translate'],
        cacheTTL: 3600 * 1000,
        debug: false,
        snapDistance: 80,              // 贴边吸附距离(px)
        snapDuration: 300,             // 吸附动画时长(ms)
        hideOffset: 28,                // 隐藏时露出的宽度/高度(px)
        hideDelay: 800,                // 贴边后延迟隐藏(ms)
        showDelay: 150,                // 悬停后延迟展开(ms)
    };

    // ======================== 状态 ========================
    const state = {
        enabled: GM_getValue('trans_enabled', true),
        targetLang: GM_getValue('trans_targetLang', CONFIG.defaultLang),
        isTranslating: false,
        totalNodes: 0,
        translatedNodes: 0,
        cache: new Map(),
        observer: null,
        snapped: false,
        snapSide: null,                // 'left' | 'right' | 'top' | 'bottom' | null
        isHidden: false,               // 是否处于隐藏状态
        hideTimer: null,
        showTimer: null,
        isHovering: false,
        toolbarX: null,
        toolbarY: null,
    };

    // ======================== 语言列表 ========================
    const LANGUAGES = {
        'zh-CN': '简体中文',
        'zh-TW': '繁体中文',
        'en': '英语',
        'ja': '日语',
        'ko': '韩语',
        'fr': '法语',
        'de': '德语',
        'es': '西班牙语',
        'pt': '葡萄牙语',
        'ru': '俄语',
        'ar': '阿拉伯语',
        'hi': '印地语',
        'it': '意大利语',
        'nl': '荷兰语',
        'pl': '波兰语',
        'vi': '越南语',
        'th': '泰语',
        'id': '印尼语',
        'ms': '马来语',
        'tr': '土耳其语',
    };

    // ======================== 样式 ========================
    GM_addStyle(`
        #trans-toolbar {
            position: fixed;
            z-index: 999999;
            background: rgba(255, 255, 255, 0.2);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border-radius: 16px;
            padding: 8px 14px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1), 0 2px 8px rgba(0, 0, 0, 0.04);
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            font-size: 13px;
            color: #1a2332;
            display: flex;
            align-items: center;
            gap: 8px;
            border: 1px solid rgba(255, 255, 255, 0.25);
            user-select: none;
            cursor: grab;
            min-width: unset;
            white-space: nowrap;
            transition: background 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease,
                        left 0.3s cubic-bezier(0.34, 1.56, 0.64, 1),
                        top 0.3s cubic-bezier(0.34, 1.56, 0.64, 1),
                        right 0.3s cubic-bezier(0.34, 1.56, 0.64, 1),
                        bottom 0.3s cubic-bezier(0.34, 1.56, 0.64, 1),
                        transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1),
                        opacity 0.3s ease;
            touch-action: none;
            will-change: transform, left, top, right, bottom;
        }
        #trans-toolbar:hover {
            background: rgba(255, 255, 255, 0.4);
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15), 0 2px 8px rgba(0, 0, 0, 0.06);
            border-color: rgba(255, 255, 255, 0.5);
        }
        #trans-toolbar.dragging {
            cursor: grabbing;
            background: rgba(255, 255, 255, 0.5);
            box-shadow: 0 12px 48px rgba(0, 0, 0, 0.2);
            transform: scale(1.02);
            transition: background 0.3s ease, box-shadow 0.3s ease, transform 0.2s ease;
        }
        #trans-toolbar.dragging.trans-snap-transition {
            transition: none !important;
        }
        /* 隐藏状态 */
        #trans-toolbar.hidden {
            opacity: 0.6;
            cursor: pointer;
        }
        #trans-toolbar.hidden .trans-logo span {
            display: none;
        }
        /* 贴边样式 */
        #trans-toolbar.snapped-right {
            border-radius: 16px 0 0 16px;
            border-right: none;
        }
        #trans-toolbar.snapped-left {
            border-radius: 0 16px 16px 0;
            border-left: none;
        }
        #trans-toolbar.snapped-top {
            border-radius: 0 0 16px 16px;
            border-top: none;
        }
        #trans-toolbar.snapped-bottom {
            border-radius: 16px 16px 0 0;
            border-bottom: none;
        }
        /* 贴边隐藏时额外样式 */
        #trans-toolbar.snapped-right.hidden {
            border-radius: 0;
            border-right: none;
            box-shadow: none;
            background: rgba(255, 255, 255, 0.08);
        }
        #trans-toolbar.snapped-left.hidden {
            border-radius: 0;
            border-left: none;
            box-shadow: none;
            background: rgba(255, 255, 255, 0.08);
        }
        #trans-toolbar.snapped-top.hidden {
            border-radius: 0;
            border-top: none;
            box-shadow: none;
            background: rgba(255, 255, 255, 0.08);
        }
        #trans-toolbar.snapped-bottom.hidden {
            border-radius: 0;
            border-bottom: none;
            box-shadow: none;
            background: rgba(255, 255, 255, 0.08);
        }
        #trans-toolbar.snapped-right.hidden:hover,
        #trans-toolbar.snapped-left.hidden:hover,
        #trans-toolbar.snapped-top.hidden:hover,
        #trans-toolbar.snapped-bottom.hidden:hover {
            background: rgba(255, 255, 255, 0.3);
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
        }
        #trans-toolbar .trans-logo {
            display: flex;
            align-items: center;
            gap: 4px;
            font-weight: 600;
            color: #1a73e8;
            font-size: 13px;
            pointer-events: none;
            flex-shrink: 0;
        }
        #trans-toolbar .trans-logo svg {
            width: 18px;
            height: 18px;
            fill: #1a73e8;
        }
        #trans-toolbar .trans-logo span {
            display: inline;
        }
        #trans-toolbar select {
            padding: 3px 8px;
            border-radius: 8px;
            border: 1px solid rgba(0, 0, 0, 0.08);
            background: rgba(255, 255, 255, 0.6);
            backdrop-filter: blur(4px);
            -webkit-backdrop-filter: blur(4px);
            font-size: 12px;
            font-weight: 500;
            color: #1a2332;
            cursor: pointer;
            outline: none;
            height: 28px;
            min-width: 68px;
            transition: background 0.2s, border-color 0.2s, opacity 0.3s ease, transform 0.3s ease;
        }
        #trans-toolbar select:hover {
            background: rgba(255, 255, 255, 0.9);
            border-color: rgba(26, 115, 232, 0.3);
        }
        #trans-toolbar.hidden select {
            opacity: 0;
            transform: scale(0.8);
            pointer-events: none;
            min-width: 0;
            width: 0;
            padding: 0;
            margin: 0;
            border: none;
            overflow: hidden;
        }
        #trans-toolbar .trans-toggle {
            position: relative;
            width: 36px;
            height: 20px;
            background: rgba(0, 0, 0, 0.15);
            border-radius: 10px;
            cursor: pointer;
            flex-shrink: 0;
            border: none;
            padding: 0;
            transition: background 0.3s, opacity 0.3s ease, transform 0.3s ease;
            touch-action: none;
        }
        #trans-toolbar .trans-toggle.active {
            background: #1a73e8;
        }
        #trans-toolbar .trans-toggle .thumb {
            position: absolute;
            top: 2px;
            left: 2px;
            width: 16px;
            height: 16px;
            background: white;
            border-radius: 50%;
            transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
            box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
        }
        #trans-toolbar .trans-toggle.active .thumb {
            transform: translateX(16px);
        }
        #trans-toolbar.hidden .trans-toggle {
            opacity: 0;
            transform: scale(0.8);
            pointer-events: none;
            width: 0;
            margin: 0;
        }
        #trans-toolbar .trans-status {
            font-size: 11px;
            color: rgba(0, 0, 0, 0.55);
            min-width: 36px;
            text-align: right;
            margin-left: 2px;
            font-weight: 500;
            pointer-events: none;
            transition: opacity 0.3s ease, transform 0.3s ease;
        }
        #trans-toolbar .trans-status.done {
            color: #1e7e34;
        }
        #trans-toolbar .trans-status.error {
            color: #d93025;
        }
        #trans-toolbar.hidden .trans-status {
            opacity: 0;
            transform: scale(0.8);
            min-width: 0;
            margin: 0;
        }
        #trans-toolbar .trans-close {
            cursor: pointer;
            color: rgba(0, 0, 0, 0.3);
            font-size: 16px;
            line-height: 1;
            padding: 0 4px;
            background: none;
            border: none;
            margin-left: 4px;
            transition: color 0.2s, transform 0.2s, opacity 0.3s ease;
            touch-action: none;
            flex-shrink: 0;
        }
        #trans-toolbar .trans-close:hover {
            color: #1a2332;
            transform: scale(1.1);
        }
        #trans-toolbar.hidden .trans-close {
            opacity: 0;
            pointer-events: none;
            width: 0;
            padding: 0;
            margin: 0;
        }
        #trans-toolbar .trans-divider {
            display: none;
        }
        /* 隐藏时的提示小标签 */
        #trans-toolbar .trans-hint {
            display: none;
            font-size: 10px;
            color: rgba(0, 0, 0, 0.3);
            writing-mode: vertical-lr;
            letter-spacing: 2px;
            font-weight: 400;
            pointer-events: none;
            padding: 2px 0;
        }
        #trans-toolbar.hidden .trans-hint {
            display: block;
        }
        #trans-toolbar.snapped-right.hidden .trans-hint {
            writing-mode: vertical-lr;
            letter-spacing: 2px;
        }
        #trans-toolbar.snapped-left.hidden .trans-hint {
            writing-mode: vertical-lr;
            letter-spacing: 2px;
        }
        #trans-toolbar.snapped-top.hidden .trans-hint {
            writing-mode: horizontal-tb;
            letter-spacing: 3px;
        }
        #trans-toolbar.snapped-bottom.hidden .trans-hint {
            writing-mode: horizontal-tb;
            letter-spacing: 3px;
        }
        .trans-progress-bar {
            position: fixed;
            top: 0;
            left: 0;
            height: 3px;
            background: linear-gradient(90deg, #1a73e8, #34a853, #fbbc04);
            z-index: 9999999;
            width: 0%;
            transition: width 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
            pointer-events: none;
            border-radius: 0 3px 3px 0;
            opacity: 0.9;
        }
        .trans-progress-bar.hide {
            opacity: 0;
            transition: opacity 0.6s ease;
        }
        .trans-translated {
            background: linear-gradient(180deg, transparent 55%, rgba(26, 115, 232, 0.06) 55%);
            border-radius: 2px;
            transition: background 0.25s;
        }
        .trans-translated:hover {
            background: linear-gradient(180deg, transparent 55%, rgba(26, 115, 232, 0.15) 55%);
        }
        @media (max-width: 600px) {
            #trans-toolbar {
                padding: 6px 10px;
                gap: 6px;
                border-radius: 14px;
                background: rgba(255, 255, 255, 0.25);
                backdrop-filter: blur(12px);
                -webkit-backdrop-filter: blur(12px);
                font-size: 12px;
                hideOffset: 20;
            }
            #trans-toolbar:hover {
                background: rgba(255, 255, 255, 0.45);
            }
            #trans-toolbar select {
                font-size: 11px;
                height: 24px;
                min-width: 56px;
                padding: 2px 6px;
            }
            #trans-toolbar .trans-toggle {
                width: 30px;
                height: 18px;
            }
            #trans-toolbar .trans-toggle .thumb {
                width: 14px;
                height: 14px;
            }
            #trans-toolbar .trans-toggle.active .thumb {
                transform: translateX(12px);
            }
            #trans-toolbar .trans-status {
                font-size: 10px;
                min-width: 24px;
            }
            #trans-toolbar .trans-logo svg {
                width: 16px;
                height: 16px;
            }
            #trans-toolbar.snapped-right {
                border-radius: 12px 0 0 12px;
            }
            #trans-toolbar.snapped-left {
                border-radius: 0 12px 12px 0;
            }
            #trans-toolbar.snapped-top {
                border-radius: 0 0 12px 12px;
            }
            #trans-toolbar.snapped-bottom {
                border-radius: 12px 12px 0 0;
            }
            #trans-toolbar .trans-hint {
                font-size: 8px;
            }
        }
    `);

    // ======================== 工具函数 ========================
    function log(...args) {
        if (CONFIG.debug) console.log('[Trans]', ...args);
    }

    function isVisible(el) {
        if (!el) return false;
        const rect = el.getBoundingClientRect();
        if (rect.width === 0 && rect.height === 0) return false;
        const style = getComputedStyle(el);
        return style.display !== 'none' && style.visibility !== 'hidden' && style.opacity !== '0';
    }

    function isExcluded(node) {
        if (!node || node.nodeType !== Node.ELEMENT_NODE) return false;
        const tag = node.tagName;
        if (CONFIG.excludeTags.includes(tag)) return true;
        for (const sel of CONFIG.excludeSelectors) {
            if (node.matches(sel)) return true;
        }
        let parent = node.parentElement;
        while (parent) {
            for (const sel of CONFIG.excludeSelectors) {
                if (parent.matches(sel)) return true;
            }
            parent = parent.parentElement;
        }
        return false;
    }

    function shouldTranslateText(text) {
        if (!text || text.trim().length === 0) return false;
        const clean = text.replace(/[\s\d\p{P}\p{S}]/gu, '');
        if (clean.length === 0) return false;
        if (text.trim().length < 2) return false;
        const emojiRegex = /^[\u{1F000}-\u{1FFFF}\u{2000}-\u{2BFF}\u{2600}-\u{27BF}\u{FE00}-\u{FEFF}]+$/u;
        if (emojiRegex.test(text.trim())) return false;
        return true;
    }

    // ======================== 缓存 ========================
    function getCacheKey(text, targetLang) {
        return `${targetLang}:${text}`;
    }

    function getCached(text, targetLang) {
        const key = getCacheKey(text, targetLang);
        const entry = state.cache.get(key);
        if (entry && Date.now() - entry.time < CONFIG.cacheTTL) {
            return entry.result;
        }
        return null;
    }

    function setCache(text, targetLang, result) {
        const key = getCacheKey(text, targetLang);
        state.cache.set(key, { result, time: Date.now() });
        if (state.cache.size > 2000) {
            const oldest = [...state.cache.entries()].sort((a, b) => a[1].time - b[1].time)[0];
            if (oldest) state.cache.delete(oldest[0]);
        }
    }

    // ======================== 翻译API ========================
    function translateText(text, targetLang) {
        return new Promise((resolve, reject) => {
            const cached = getCached(text, targetLang);
            if (cached !== null) {
                log('Cache hit:', text.slice(0, 30) + '...');
                resolve(cached);
                return;
            }

            const url = `https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=${targetLang}&dt=t&q=${encodeURIComponent(text)}`;
            log('Translating:', text.slice(0, 50) + '...');

            GM_xmlhttpRequest({
                method: 'GET',
                url: url,
                timeout: 15000,
                onload: function (resp) {
                    try {
                        const data = JSON.parse(resp.responseText);
                        let result = '';
                        if (data && Array.isArray(data) && data[0] && Array.isArray(data[0])) {
                            for (const part of data[0]) {
                                if (part && part[0]) {
                                    result += part[0];
                                }
                            }
                        }
                        if (result) {
                            setCache(text, targetLang, result);
                            resolve(result);
                        } else {
                            reject(new Error('No translation result'));
                        }
                    } catch (e) {
                        reject(e);
                    }
                },
                onerror: function (err) {
                    reject(err);
                },
                ontimeout: function () {
                    reject(new Error('Timeout'));
                }
            });
        });
    }

    // ======================== 文本节点收集 ========================
    function getTextNodes(root) {
        const walker = document.createTreeWalker(
            root,
            NodeFilter.SHOW_TEXT,
            {
                acceptNode: function (node) {
                    const parent = node.parentElement;
                    if (!parent) return NodeFilter.FILTER_REJECT;
                    if (isExcluded(parent)) return NodeFilter.FILTER_REJECT;
                    if (!isVisible(parent)) return NodeFilter.FILTER_REJECT;
                    const text = node.textContent;
                    if (!shouldTranslateText(text)) return NodeFilter.FILTER_REJECT;
                    if (parent.dataset.translated === 'true') return NodeFilter.FILTER_REJECT;
                    return NodeFilter.FILTER_ACCEPT;
                }
            },
            false
        );
        const nodes = [];
        let node;
        while ((node = walker.nextNode())) {
            nodes.push(node);
        }
        return nodes;
    }

    // ======================== 并发控制 ========================
    async function asyncPool(poolLimit, tasks) {
        const results = [];
        const executing = [];
        for (const task of tasks) {
            const p = Promise.resolve().then(() => task());
            results.push(p);
            if (poolLimit <= tasks.length) {
                const e = p.then(() => executing.splice(executing.indexOf(e), 1));
                executing.push(e);
                if (executing.length >= poolLimit) {
                    await Promise.race(executing);
                }
            }
        }
        return Promise.all(results);
    }

    // ======================== 批量翻译 ========================
    async function translateNodes(nodes, targetLang, onProgress) {
        if (!nodes || nodes.length === 0) return;

        state.totalNodes = nodes.length;
        state.translatedNodes = 0;
        state.isTranslating = true;
        updateUI();

        const groups = new Map();
        for (const node of nodes) {
            const parent = node.parentElement;
            if (!parent) continue;
            if (!groups.has(parent)) {
                groups.set(parent, []);
            }
            groups.get(parent).push(node);
        }

        const groupList = [...groups.values()];
        let completed = 0;
        const totalGroups = groupList.length;

        const tasks = groupList.map((childNodes) => {
            return async () => {
                try {
                    const texts = childNodes.map(n => n.textContent);
                    const fullText = texts.join('|~|');
                    if (fullText.length > CONFIG.maxTextLength * 2) {
                        const batchSize = Math.ceil(fullText.length / CONFIG.maxTextLength);
                        const parts = [];
                        for (let i = 0; i < childNodes.length; i += batchSize) {
                            const batch = childNodes.slice(i, i + batchSize);
                            const batchTexts = batch.map(n => n.textContent);
                            const combined = batchTexts.join('|~|');
                            try {
                                const result = await translateText(combined, targetLang);
                                parts.push({ batch, result });
                            } catch (e) {
                                log('Batch error:', e);
                            }
                        }
                        for (const { batch, result } of parts) {
                            if (result) {
                                const segs = result.split('|~|');
                                for (let j = 0; j < batch.length && j < segs.length; j++) {
                                    const node = batch[j];
                                    const parent = node.parentElement;
                                    if (parent && !parent.dataset.translated) {
                                        const newText = segs[j] || node.textContent;
                                        if (newText && newText !== node.textContent) {
                                            node.textContent = newText;
                                            parent.dataset.translated = 'true';
                                            parent.classList.add('trans-translated');
                                        }
                                        state.translatedNodes++;
                                    }
                                }
                            }
                        }
                    } else {
                        const translated = await translateText(fullText, targetLang);
                        if (translated) {
                            const parts = translated.split('|~|');
                            for (let i = 0; i < childNodes.length && i < parts.length; i++) {
                                const node = childNodes[i];
                                const parent = node.parentElement;
                                if (parent && !parent.dataset.translated) {
                                    const newText = parts[i] || node.textContent;
                                    if (newText && newText !== node.textContent) {
                                        node.textContent = newText;
                                        parent.dataset.translated = 'true';
                                        parent.classList.add('trans-translated');
                                    }
                                    state.translatedNodes++;
                                }
                            }
                        }
                    }
                } catch (e) {
                    log('Group translation error:', e);
                } finally {
                    completed++;
                    if (onProgress) {
                        onProgress(Math.min(1, completed / totalGroups));
                    }
                }
            };
        });

        await asyncPool(CONFIG.maxConcurrent, tasks);

        state.isTranslating = false;
        updateUI();
        log('Translation completed, nodes:', state.translatedNodes);
    }

    // ======================== 主翻译流程 ========================
    async function translatePage(targetLang, force = false) {
        if (!state.enabled) return;
        if (state.isTranslating && !force) {
            log('Already translating, skip');
            return;
        }

        log('Starting translation to:', targetLang);
        showProgress(0);

        const root = document.body;
        if (!root) return;

        const nodes = getTextNodes(root);
        log('Found text nodes:', nodes.length);
        if (nodes.length === 0) {
            showProgress(1);
            updateStatus('done', '无内容可翻译');
            return;
        }

        await translateNodes(nodes, targetLang, (progress) => {
            showProgress(progress);
        });

        showProgress(1);
        updateStatus('done', `已翻译 ${state.translatedNodes} 个文本`);
        setTimeout(() => {
            const bar = document.querySelector('.trans-progress-bar');
            if (bar) bar.classList.add('hide');
        }, 1500);
    }

    // ======================== DOM观察 ========================
    function setupObserver() {
        if (state.observer) {
            state.observer.disconnect();
        }

        state.observer = new MutationObserver((mutations) => {
            if (!state.enabled || state.isTranslating) return;
            let hasNewContent = false;
            for (const mutation of mutations) {
                if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
                    for (const node of mutation.addedNodes) {
                        if (node.nodeType === Node.ELEMENT_NODE) {
                            const texts = getTextNodes(node);
                            if (texts.length > 0) {
                                hasNewContent = true;
                                break;
                            }
                        }
                    }
                }
                if (mutation.type === 'characterData') {
                    const parent = mutation.target.parentElement;
                    if (parent && !parent.dataset.translated) {
                        hasNewContent = true;
                        break;
                    }
                }
                if (hasNewContent) break;
            }
            if (hasNewContent) {
                log('New content detected, translating...');
                clearTimeout(window._transDebounce);
                window._transDebounce = setTimeout(() => {
                    translatePage(state.targetLang);
                }, 600);
            }
        });

        state.observer.observe(document.body, {
            childList: true,
            subtree: true,
            characterData: true,
            characterDataOldValue: false,
        });
        log('Observer started');
    }

    // ======================== 贴边隐藏系统 ========================
    function setupSnapAndHideSystem(toolbar) {
        let snapTimeout = null;
        const hideOffset = CONFIG.hideOffset;

        // 检测并执行贴边
        function checkAndSnap(instant = false) {
            const rect = toolbar.getBoundingClientRect();
            const winWidth = window.innerWidth;
            const winHeight = window.innerHeight;
            const snapDist = CONFIG.snapDistance;

            const isDragging = toolbar.classList.contains('dragging');
            if (isDragging) return false;

            const distLeft = rect.left;
            const distRight = winWidth - rect.right;
            const distTop = rect.top;
            const distBottom = winHeight - rect.bottom;

            const edges = [
                { side: 'left', dist: distLeft },
                { side: 'right', dist: distRight },
                { side: 'top', dist: distTop },
                { side: 'bottom', dist: distBottom },
            ];
            edges.sort((a, b) => a.dist - b.dist);
            const nearest = edges[0];

            if (nearest.dist < snapDist) {
                const side = nearest.side;
                let targetX, targetY;

                // 计算贴边位置（完全贴边，为隐藏做准备）
                switch (side) {
                    case 'left':
                        targetX = 0;
                        targetY = Math.max(0, Math.min(rect.top, winHeight - rect.height));
                        break;
                    case 'right':
                        targetX = winWidth - rect.width;
                        targetY = Math.max(0, Math.min(rect.top, winHeight - rect.height));
                        break;
                    case 'top':
                        targetX = Math.max(0, Math.min(rect.left, winWidth - rect.width));
                        targetY = 0;
                        break;
                    case 'bottom':
                        targetX = Math.max(0, Math.min(rect.left, winWidth - rect.width));
                        targetY = winHeight - rect.height;
                        break;
                }

                // 保存贴边状态
                state.snapSide = side;
                state.snapped = true;
                state.toolbarX = targetX;
                state.toolbarY = targetY;

                // 更新样式
                updateSnapState(side);

                // 移动位置
                applyPosition(targetX, targetY, instant);

                // 触发隐藏（延迟）
                scheduleHide();

                return true;
            } else {
                // 不贴边
                clearSnapState();
                state.snapped = false;
                state.snapSide = null;
                state.isHidden = false;
                toolbar.classList.remove('hidden');
                cancelHide();
                return false;
            }
        }

        function applyPosition(x, y, instant = false) {
            if (instant) {
                toolbar.style.transition = 'none';
            }
            toolbar.style.left = x + 'px';
            toolbar.style.top = y + 'px';
            toolbar.style.right = 'auto';
            toolbar.style.bottom = 'auto';
            if (instant) {
                setTimeout(() => {
                    toolbar.style.transition = '';
                }, 50);
            }
        }

        function updateSnapState(side) {
            toolbar.classList.remove('snapped-left', 'snapped-right', 'snapped-top', 'snapped-bottom');
            toolbar.classList.add('snapped-' + side);
        }

        function clearSnapState() {
            toolbar.classList.remove('snapped-left', 'snapped-right', 'snapped-top', 'snapped-bottom');
        }

        // ----- 隐藏逻辑 -----
        function scheduleHide() {
            cancelHide();
            if (!state.snapped || state.isHovering) return;
            state.hideTimer = setTimeout(() => {
                doHide();
            }, CONFIG.hideDelay);
        }

        function cancelHide() {
            if (state.hideTimer) {
                clearTimeout(state.hideTimer);
                state.hideTimer = null;
            }
        }

        function doHide() {
            if (!state.snapped || state.isHovering) return;
            state.isHidden = true;
            toolbar.classList.add('hidden');

            // 根据贴边方向移动工具栏，只露出一小部分
            const rect = toolbar.getBoundingClientRect();
            const winWidth = window.innerWidth;
            const winHeight = window.innerHeight;
            const side = state.snapSide;

            let targetX = rect.left;
            let targetY = rect.top;

            switch (side) {
                case 'left':
                    targetX = -(rect.width - hideOffset);
                    break;
                case 'right':
                    targetX = winWidth - hideOffset;
                    break;
                case 'top':
                    targetY = -(rect.height - hideOffset);
                    break;
                case 'bottom':
                    targetY = winHeight - hideOffset;
                    break;
            }

            toolbar.style.transition = `left ${CONFIG.snapDuration}ms cubic-bezier(0.34, 1.56, 0.64, 1),
                                       top ${CONFIG.snapDuration}ms cubic-bezier(0.34, 1.56, 0.64, 1)`;
            toolbar.style.left = targetX + 'px';
            toolbar.style.top = targetY + 'px';
            toolbar.style.right = 'auto';
            toolbar.style.bottom = 'auto';

            setTimeout(() => {
                toolbar.style.transition = '';
            }, CONFIG.snapDuration + 50);
        }

        function showToolbar() {
            cancelHide();
            if (!state.snapped) return;

            state.isHidden = false;
            toolbar.classList.remove('hidden');

            // 恢复到贴边位置
            const side = state.snapSide;
            const winWidth = window.innerWidth;
            const winHeight = window.innerHeight;
            const rect = toolbar.getBoundingClientRect();
            const toolbarWidth = rect.width;
            const toolbarHeight = rect.height;

            let targetX = rect.left;
            let targetY = rect.top;

            // 恢复到完全贴边位置
            switch (side) {
                case 'left':
                    targetX = 0;
                    targetY = Math.max(0, Math.min(rect.top, winHeight - toolbarHeight));
                    break;
                case 'right':
                    targetX = winWidth - toolbarWidth;
                    targetY = Math.max(0, Math.min(rect.top, winHeight - toolbarHeight));
                    break;
                case 'top':
                    targetX = Math.max(0, Math.min(rect.left, winWidth - toolbarWidth));
                    targetY = 0;
                    break;
                case 'bottom':
                    targetX = Math.max(0, Math.min(rect.left, winWidth - toolbarWidth));
                    targetY = winHeight - toolbarHeight;
                    break;
            }

            toolbar.style.transition = `left ${CONFIG.showDelay}ms ease, top ${CONFIG.showDelay}ms ease`;
            toolbar.style.left = targetX + 'px';
            toolbar.style.top = targetY + 'px';
            toolbar.style.right = 'auto';
            toolbar.style.bottom = 'auto';

            state.toolbarX = targetX;
            state.toolbarY = targetY;

            setTimeout(() => {
                toolbar.style.transition = '';
            }, CONFIG.showDelay + 50);

            // 延迟后重新隐藏（如果仍然贴边且没有悬停）
            setTimeout(() => {
                if (!state.isHovering && state.snapped) {
                    scheduleHide();
                }
            }, 300);
        }

        // ----- 悬停事件 -----
        function onMouseEnter() {
            state.isHovering = true;
            cancelHide();
            if (state.isHidden) {
                showToolbar();
            }
        }

        function onMouseLeave() {
            state.isHovering = false;
            // 延迟一下再隐藏，防止误触
            setTimeout(() => {
                if (!state.isHovering && state.snapped && !toolbar.classList.contains('dragging')) {
                    scheduleHide();
                }
            }, 200);
        }

        // ----- 外部调用 -----
        function unsnap() {
            if (state.snapped) {
                clearSnapState();
                state.snapped = false;
                state.snapSide = null;
                state.isHidden = false;
                toolbar.classList.remove('hidden');
                cancelHide();
            }
        }

        function scheduleSnapCheck(instant = false) {
            clearTimeout(snapTimeout);
            snapTimeout = setTimeout(() => {
                checkAndSnap(instant);
            }, 150);
        }

        function onResize() {
            if (state.snapped) {
                // 重新计算贴边位置
                const rect = toolbar.getBoundingClientRect();
                const side = state.snapSide;
                const winWidth = window.innerWidth;
                const winHeight = window.innerHeight;
                const toolbarWidth = rect.width;
                const toolbarHeight = rect.height;

                let targetX = rect.left;
                let targetY = rect.top;

                switch (side) {
                    case 'left':
                        targetX = 0;
                        targetY = Math.max(0, Math.min(rect.top, winHeight - toolbarHeight));
                        break;
                    case 'right':
                        targetX = winWidth - toolbarWidth;
                        targetY = Math.max(0, Math.min(rect.top, winHeight - toolbarHeight));
                        break;
                    case 'top':
                        targetX = Math.max(0, Math.min(rect.left, winWidth - toolbarWidth));
                        targetY = 0;
                        break;
                    case 'bottom':
                        targetX = Math.max(0, Math.min(rect.left, winWidth - toolbarWidth));
                        targetY = winHeight - toolbarHeight;
                        break;
                }

                if (state.isHidden) {
                    // 隐藏状态下，需要重新计算隐藏位置
                    switch (side) {
                        case 'left':
                            targetX = -(toolbarWidth - hideOffset);
                            break;
                        case 'right':
                            targetX = winWidth - hideOffset;
                            break;
                        case 'top':
                            targetY = -(toolbarHeight - hideOffset);
                            break;
                        case 'bottom':
                            targetY = winHeight - hideOffset;
                            break;
                    }
                }

                applyPosition(targetX, targetY, true);
                state.toolbarX = targetX;
                state.toolbarY = targetY;
            } else {
                checkAndSnap(true);
            }
        }

        function snapNow() {
            checkAndSnap(true);
        }

        // 绑定事件
        toolbar.addEventListener('mouseenter', onMouseEnter);
        toolbar.addEventListener('mouseleave', onMouseLeave);

        // 页面点击时如果工具栏隐藏，点击展开
        toolbar.addEventListener('click', (e) => {
            if (state.isHidden) {
                e.stopPropagation();
                showToolbar();
                // 阻止对内部元素的点击
                return;
            }
        });

        // 初次加载时检查
        setTimeout(() => {
            snapNow();
        }, 200);

        return {
            checkAndSnap,
            unsnap,
            scheduleSnapCheck,
            onResize,
            snapNow,
            showToolbar,
            doHide,
            scheduleHide,
            cancelHide,
        };
    }

    // ======================== 拖拽系统（集成贴边隐藏） ========================
    function setupDragSystem(toolbar) {
        let isDragging = false;
        let startX = 0;
        let startY = 0;
        let originalX = 0;
        let originalY = 0;
        let hasMoved = false;

        const snap = setupSnapAndHideSystem(toolbar);

        function getPosition() {
            const rect = toolbar.getBoundingClientRect();
            return {
                x: rect.left,
                y: rect.top,
                width: rect.width,
                height: rect.height
            };
        }

        function setPosition(x, y, noTransition = false) {
            if (noTransition) {
                toolbar.style.transition = 'none';
            }
            toolbar.style.left = x + 'px';
            toolbar.style.top = y + 'px';
            toolbar.style.right = 'auto';
            toolbar.style.bottom = 'auto';
            if (noTransition) {
                setTimeout(() => {
                    toolbar.style.transition = '';
                }, 50);
            }
        }

        function constrainPosition(x, y) {
            const maxX = window.innerWidth - toolbar.offsetWidth - 5;
            const maxY = window.innerHeight - toolbar.offsetHeight - 5;
            return {
                x: Math.max(5, Math.min(x, maxX)),
                y: Math.max(5, Math.min(y, maxY))
            };
        }

        function onDragStart(e) {
            const target = e.target;
            if (target.closest('select') ||
                target.closest('.trans-toggle') ||
                target.closest('.trans-close') ||
                target.closest('button')) {
                return;
            }

            // 如果处于隐藏状态，先展开再拖拽
            if (state.isHidden) {
                snap.showToolbar();
                // 稍等动画完成再开始拖拽
                setTimeout(() => {
                    startDrag(e);
                }, 150);
                return;
            }

            startDrag(e);
        }

        function startDrag(e) {
            snap.unsnap();
            snap.cancelHide();

            const pos = getPosition();
            startX = e.clientX || e.touches?.[0]?.clientX || 0;
            startY = e.clientY || e.touches?.[0]?.clientY || 0;
            originalX = pos.x;
            originalY = pos.y;
            hasMoved = false;

            isDragging = true;
            toolbar.classList.add('dragging');
            toolbar.classList.add('trans-snap-transition');

            e.preventDefault();
        }

        function onDragMove(e) {
            if (!isDragging) return;

            const clientX = e.clientX || e.touches?.[0]?.clientX || 0;
            const clientY = e.clientY || e.touches?.[0]?.clientY || 0;

            const deltaX = clientX - startX;
            const deltaY = clientY - startY;

            if (Math.abs(deltaX) > 3 || Math.abs(deltaY) > 3) {
                hasMoved = true;
            }

            if (!hasMoved) return;

            let newX = originalX + deltaX;
            let newY = originalY + deltaY;

            const constrained = constrainPosition(newX, newY);
            setPosition(constrained.x, constrained.y);

            e.preventDefault();
        }

        function onDragEnd(e) {
            if (isDragging) {
                isDragging = false;
                toolbar.classList.remove('dragging');
                toolbar.classList.remove('trans-snap-transition');
                toolbar.style.transition = '';

                snap.scheduleSnapCheck();
            }
        }

        // 鼠标事件
        toolbar.addEventListener('mousedown', onDragStart);
        document.addEventListener('mousemove', onDragMove);
        document.addEventListener('mouseup', onDragEnd);

        // 触摸事件
        toolbar.addEventListener('touchstart', onDragStart, { passive: false });
        document.addEventListener('touchmove', onDragMove, { passive: false });
        document.addEventListener('touchend', onDragEnd);
        document.addEventListener('touchcancel', onDragEnd);

        // 窗口事件
        const resizeHandler = () => { snap.onResize(); };
        window.addEventListener('resize', resizeHandler);

        return function cleanup() {
            toolbar.removeEventListener('mousedown', onDragStart);
            document.removeEventListener('mousemove', onDragMove);
            document.removeEventListener('mouseup', onDragEnd);
            toolbar.removeEventListener('touchstart', onDragStart);
            document.removeEventListener('touchmove', onDragMove);
            document.removeEventListener('touchend', onDragEnd);
            document.removeEventListener('touchcancel', onDragEnd);
            window.removeEventListener('resize', resizeHandler);
        };
    }

    // ======================== UI ========================
    let uiElements = {};
    let dragCleanup = null;

    function createUI() {
        const old = document.getElementById('trans-toolbar');
        if (old) {
            if (dragCleanup) dragCleanup();
            old.remove();
        }

        const toolbar = document.createElement('div');
        toolbar.id = 'trans-toolbar';

        toolbar.innerHTML = `
            <div class="trans-logo">
                <svg viewBox="0 0 24 24" width="16" height="16"><path d="M12.87 15.07l-2.54-2.51.03-.03c1.74-1.94 2.98-4.17 3.71-6.53H17V4h-7V2H8v2H1v1.99h11.17C11.5 7.92 10.44 9.75 9 11.35 8.07 10.32 7.3 9.19 6.69 8h-2c.73 1.63 1.73 3.17 2.98 4.56l-5.09 5.02L4 19l5-5 3.11 3.11.76-2.04zM18.5 10h-2L12 22h2l1.12-3h4.75L21 22h2l-4.5-12zm-2.62 7l1.62-4.33L19.12 17h-3.24z"/></svg>
                <span>翻译</span>
            </div>
            <span class="trans-hint">翻 译</span>
            <div class="trans-divider"></div>
            <select id="trans-lang-select">
                ${Object.entries(LANGUAGES).map(([code, name]) =>
                    `<option value="${code}" ${code === state.targetLang ? 'selected' : ''}>${name}</option>`
                ).join('')}
            </select>
            <button class="trans-toggle ${state.enabled ? 'active' : ''}" id="trans-toggle-btn">
                <span class="thumb"></span>
            </button>
            <span class="trans-status" id="trans-status">${state.enabled ? '就绪' : '已停用'}</span>
            <button class="trans-close" id="trans-close-btn" title="关闭工具栏">✕</button>
        `;

        document.body.appendChild(toolbar);

        const progressBar = document.createElement('div');
        progressBar.className = 'trans-progress-bar hide';
        progressBar.id = 'trans-progress-bar';
        document.body.appendChild(progressBar);

        uiElements = {
            toolbar,
            langSelect: document.getElementById('trans-lang-select'),
            toggleBtn: document.getElementById('trans-toggle-btn'),
            status: document.getElementById('trans-status'),
            closeBtn: document.getElementById('trans-close-btn'),
            progressBar: document.getElementById('trans-progress-bar'),
        };

        dragCleanup = setupDragSystem(toolbar);

        uiElements.langSelect.addEventListener('change', (e) => {
            const lang = e.target.value;
            state.targetLang = lang;
            GM_setValue('trans_targetLang', lang);
            updateStatus('info', '切换语言...');
            setTimeout(() => {
                translatePage(lang, true);
            }, 300);
        });

        uiElements.toggleBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            state.enabled = !state.enabled;
            GM_setValue('trans_enabled', state.enabled);
            uiElements.toggleBtn.classList.toggle('active', state.enabled);
            if (state.enabled) {
                updateStatus('info', '启用...');
                setTimeout(() => translatePage(state.targetLang, true), 400);
            } else {
                updateStatus('info', '已停用');
                document.querySelectorAll('[data-translated="true"]').forEach(el => {
                    delete el.dataset.translated;
                    el.classList.remove('trans-translated');
                });
                location.reload();
            }
        });

        uiElements.closeBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            uiElements.toolbar.style.display = 'none';
        });

        log('UI created with auto-hide snap system');
    }

    function updateUI() {
        if (uiElements.toggleBtn) {
            uiElements.toggleBtn.classList.toggle('active', state.enabled);
        }
        if (uiElements.langSelect) {
            uiElements.langSelect.value = state.targetLang;
        }
    }

    function updateStatus(type, msg) {
        const el = uiElements.status;
        if (!el) return;
        el.textContent = msg;
        el.className = 'trans-status';
        if (type === 'done') el.classList.add('done');
        if (type === 'error') el.classList.add('error');
    }

    function showProgress(value) {
        const bar = document.getElementById('trans-progress-bar');
        if (!bar) return;
        if (value >= 1) {
            bar.style.width = '100%';
            setTimeout(() => bar.classList.add('hide'), 500);
        } else {
            bar.classList.remove('hide');
            bar.style.width = Math.min(100, value * 100) + '%';
        }
    }

    // ======================== 初始化 ========================
    function init() {
        log('Initializing...');
        createUI();

        if (state.enabled) {
            if (document.readyState === 'complete') {
                setTimeout(() => translatePage(state.targetLang), 600);
            } else {
                window.addEventListener('load', () => {
                    setTimeout(() => translatePage(state.targetLang), 800);
                });
            }
        }

        setupObserver();

        let lastUrl = location.href;
        setInterval(() => {
            if (location.href !== lastUrl) {
                lastUrl = location.href;
                log('URL changed, re-translating...');
                if (state.enabled) {
                    setTimeout(() => translatePage(state.targetLang, true), 1000);
                }
            }
        }, 1500);

        log('Initialized');
    }

    if (document.body) {
        init();
    } else {
        document.addEventListener('DOMContentLoaded', init);
    }

})();