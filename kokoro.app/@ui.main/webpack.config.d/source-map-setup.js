// See, https://webpack.js.org/configuration/devtool/
//
// NOTE: `hidden-*` generates source maps without adding the `sourceMappingURL`
// comment. See also HTTP header `SourceMap` -- https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/SourceMap
config.devtool = 'hidden-nosources-source-map'

// See, https://webpack.js.org/configuration/output/#outputdevtoolmodulefilenametemplate
//
// The setup below achieves the following:
// - Strips away any leading "../" or "./" from each source's resource path.
// - Ensures that each source entry is not an absolute URL, which also prevents
// the "webpack:" scheme (or similar) from being included.
config.output.devtoolModuleFilenameTemplate =
	info => info.resourcePath.replace(/^(?:\.\.?\/)+/, "")
