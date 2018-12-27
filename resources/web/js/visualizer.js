$(document).ready(function () {
	window.visualize = function (traceJSON) {
		var opts = {
			hideCode: true,
			hideOutput: true,
			disableHeapNesting: true,
			textualMemoryLabels: false,
			visualizerIdOverride: "1",
			startingInstruction: 0,
			lang: "java"
		};
		var trace = JSON.parse(traceJSON);
		var container = {
			code: "",
			stdin: "",
			trace: trace
		};
		$('#root').empty();
		var v = new ExecutionVisualizer('root', container, opts);
	};
	window.status = "Ready";
});