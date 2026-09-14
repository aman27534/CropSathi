sed -i '/\/\/ Crop Growth Trend Chart/i \
        // Soil Maintenance Tips\
        Surface(\
            modifier = Modifier.fillMaxWidth(),\
            shape = RoundedCornerShape(16.dp),\
            color = MaterialTheme.colorScheme.tertiaryContainer,\
            shadowElevation = 2.dp\
        ) {\
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {\
                Row(verticalAlignment = Alignment.CenterVertically) {\
                    Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)\
                    Spacer(modifier = Modifier.width(8.dp))\
                    Text("Soil Maintenance Tip", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)\
                }\
                Text(\
                    text = when (soilTexture) {\
                        "Sand" -> "Sandy soil drains quickly. Consider adding organic matter (like compost) to improve water retention and reduce irrigation frequency."\
                        "Clay" -> "Clay soil retains water well but can become compacted. Aerate regularly and avoid overwatering to prevent root rot."\
                        else -> "Loam is ideal for agriculture. Maintain a regular mulching schedule to preserve topsoil moisture and prevent erosion."\
                    },\
                    style = MaterialTheme.typography.bodyMedium,\
                    color = MaterialTheme.colorScheme.onTertiaryContainer\
                )\
            }\
        }\
' app/src/main/java/com/example/MainActivity.kt
