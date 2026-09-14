import sys

def remove_duplicates():
    with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
        lines = f.readlines()
        
    seen_imports = set()
    new_lines = []
    
    for line in lines:
        if line.startswith("import "):
            if line in seen_imports:
                continue
            seen_imports.add(line)
        new_lines.append(line)
        
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
        f.writelines(new_lines)

remove_duplicates()
