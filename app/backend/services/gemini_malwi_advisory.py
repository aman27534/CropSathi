import google.generativeai as genai
from ..config import settings

# Initialize Gemini Model
# Ensure GEMINI_API_KEY is configured in your environment or secrets manager
genai.configure(api_key=settings.GEMINI_API_KEY)
model = genai.GenerativeModel('gemini-1.5-flash')

async def generate_agri_advice(disease_name: str, language: str):
    base_prompt = f"You are an expert agro-doctor. The user has detected a high probability of '{disease_name}' on their crop."
    
    if language.lower() == 'malwi':
        system_instructions = """
        Provide the advisory in the Malwi dialect of Madhya Pradesh. 
        Enforce the use of regional Malwa dialect terms like:
        - इल्ली (Caterpillar)
        - खात नाखनो (Applying fertilizer)
        - दवाई को छांटव (Foliar spray)
        Ensure the tone is helpful, deeply empathetic, and directly actionable by local farmers.
        """
    elif language.lower() == 'hindi':
        system_instructions = "Provide the advisory in standard Hindi."
    else:
        system_instructions = "Provide the advisory in standard English."

    full_prompt = f"{base_prompt}\n{system_instructions}"

    # We use generate_content_async to prevent blocking the FastAPI event loop
    response = await model.generate_content_async(full_prompt)
    
    return {
        "disease": disease_name,
        "language": language, 
        "advisory": response.text
    }
