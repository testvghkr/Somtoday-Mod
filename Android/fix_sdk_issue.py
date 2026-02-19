import os
import re

# We gaan de build-bestanden aanpassen naar SDK 34 (Stabieler voor servers)
gradle_path = 'app/build.gradle.kts' if os.path.exists('app/build.gradle.kts') else 'app/build.gradle'

if os.path.exists(gradle_path):
    with open(gradle_path, 'r') as f:
        content = f.read()
    
    # Vervang SDK 36 door 34
    content = re.sub(r'compileSdk\s*=?\s*36', 'compileSdk = 34', content)
    content = re.sub(r'targetSdk\s*=?\s*36', 'targetSdk = 34', content)
    # Zorg dat de build tools ook op een bekende versie staan
    content = re.sub(r'buildToolsVersion\s*=?\s*"[^"]+"', 'buildToolsVersion = "34.0.0"', content)
    
    with open(gradle_path, 'w') as f:
        f.write(content)
    print(f"✅ {gradle_path} aangepast naar SDK 34.")
