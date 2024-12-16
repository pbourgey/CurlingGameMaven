clc; clear; close all;
I = imread('../captured/captured3.jpg');

% Réorienter l'image pour qu'elle soit verticale
if size(I, 2) > size(I, 1)
    I = imrotate(I, 90);
end

% marge autour de l'image
margin_size = 20;
I_with_margin = padarray(I, [margin_size, margin_size], 255, 'both');
[height, width, ~] = size(I_with_margin);

% cible
target_x = width / 2;
target_y = margin_size + 140;

% --- aire de jeu blanche avec rectangle rouge et cible ---

blank_image = 255 * ones(height, width, 'uint8');
figure; imshow(blank_image); hold on;
rectangle('Position', [margin_size+1, margin_size+1, width - 2*margin_size, height - 2*margin_size], ...
    'EdgeColor', 'r', 'LineWidth', 2);
plot(target_x, target_y, 'rx', 'MarkerSize', 15, 'LineWidth', 2);
title('Aire de jeu avec rectangle rouge et cible');

I_gray = rgb2gray(I_with_margin);
% Binariser l'image (jeton noir sur fond blanc)
BW = imbinarize(I_gray, 'adaptive', 'ForegroundPolarity', 'dark', 'Sensitivity', 0.4);

% Inverser l'image binaire pour que le jeton noir devient blanc
BW = ~BW;


BW_clean = bwareaopen(BW, 500);
props = regionprops(BW_clean, 'Centroid', 'Area', 'Perimeter', 'BoundingBox');


max_area = 0;
jeton_idx = 0;
for i = 1:length(props)
    area = props(i).Area;
    if area > max_area
        max_area = area;
        jeton_idx = i;
    end
end

if jeton_idx == 0
    error('Aucun objet noir détecté dans l''image.');
end

jeton_centroid = props(jeton_idx).Centroid;
jeton_bbox = props(jeton_idx).BoundingBox;


distance_pixels = sqrt((jeton_centroid(1) - target_x)^2 + (jeton_centroid(2) - target_y)^2);

% --- affichage final ---
figure; imshow(I_with_margin); hold on;
rectangle('Position', [margin_size+1, margin_size+1, width - 2*margin_size, height - 2*margin_size], ...
    'EdgeColor', 'r', 'LineWidth', 2);
plot(jeton_centroid(1), jeton_centroid(2), 'bo', 'MarkerSize', 15, 'LineWidth', 2);
rectangle('Position', jeton_bbox, 'EdgeColor', 'b', 'LineWidth', 2);
plot(target_x, target_y, 'rx', 'MarkerSize', 15, 'LineWidth', 2);
line([jeton_centroid(1), target_x], [jeton_centroid(2), target_y], 'Color', 'y', 'LineWidth', 2);
text_position_x = (jeton_centroid(1) + target_x) / 2;
text_position_y = (jeton_centroid(2) + target_y) / 2;
distance_text = sprintf('Distance: %.2f pixels', distance_pixels);
text(text_position_x, text_position_y, distance_text, 'Color', 'y', 'FontSize', 12, 'HorizontalAlignment', 'center');
title('Affichage final : jeton, cible, et distance');
