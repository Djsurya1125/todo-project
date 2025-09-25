#!/bin/bash

# Create simple placeholder launcher icons using ImageMagick (if available)
# This creates basic colored squares as placeholders

ICON_COLOR="#6200EE"

# Function to create a simple colored icon
create_icon() {
    local size=$1
    local output_dir=$2
    local filename=$3
    
    mkdir -p "$output_dir"
    
    # Try to use ImageMagick if available, otherwise create a simple XML drawable
    if command -v convert &> /dev/null; then
        convert -size ${size}x${size} xc:"$ICON_COLOR" "$output_dir/$filename"
    else
        # Create a simple XML drawable as fallback
        cat > "$output_dir/${filename%.png}.xml" << EOF
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="$ICON_COLOR" />
    <corners android:radius="8dp" />
</shape>
EOF
    fi
}

# Create icons for different densities
create_icon 48 "app/src/main/res/mipmap-mdpi" "ic_launcher.png"
create_icon 48 "app/src/main/res/mipmap-mdpi" "ic_launcher_round.png"

create_icon 72 "app/src/main/res/mipmap-hdpi" "ic_launcher.png"
create_icon 72 "app/src/main/res/mipmap-hdpi" "ic_launcher_round.png"

create_icon 96 "app/src/main/res/mipmap-xhdpi" "ic_launcher.png"
create_icon 96 "app/src/main/res/mipmap-xhdpi" "ic_launcher_round.png"

create_icon 144 "app/src/main/res/mipmap-xxhdpi" "ic_launcher.png"
create_icon 144 "app/src/main/res/mipmap-xxhdpi" "ic_launcher_round.png"

create_icon 192 "app/src/main/res/mipmap-xxxhdpi" "ic_launcher.png"
create_icon 192 "app/src/main/res/mipmap-xxxhdpi" "ic_launcher_round.png"

echo "Launcher icons created (or XML fallbacks if ImageMagick not available)"
