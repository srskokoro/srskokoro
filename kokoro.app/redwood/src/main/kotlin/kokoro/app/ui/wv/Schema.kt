package kokoro.app.ui.wv

import app.cash.redwood.layout.RedwoodLayout
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Schema.Dependency

@Schema(
	members = [
	],
	dependencies = [
		Dependency(1, RedwoodLayout::class),
	],
)
interface Schema
