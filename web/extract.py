import fitz  # PyMuPDF
import os

pdf_path = r"C:\Users\zaid khan\Downloads\ss.pdf"
output_dir = r"C:\Users\zaid khan\OneDrive\Desktop\AiCareerPilot\web\public\screenshots"

if not os.path.exists(output_dir):
    os.makedirs(output_dir)

doc = fitz.open(pdf_path)

# Based on the PDF pages provided:
# Page 0 (1): Dashboard
# Page 1 (2): Interview Simulator
# Page 2 (3): Career Roadmap
# Page 3 (4): AI Test Console (Evaluation)
filenames = ["dashboard.png", "interview.png", "roadmap.png", "evaluation.png"]

for page_index in range(min(len(doc), len(filenames))):
    page = doc[page_index]
    # Render the page to a pixmap (image)
    pix = page.get_pixmap(dpi=300)
    output_path = os.path.join(output_dir, filenames[page_index])
    pix.save(output_path)
    print(f"Saved: {output_path}")

print("Extraction complete!")
