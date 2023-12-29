import os
import openai
import logging

openai.api_key = os.getenv('OPENAI_API_KEY')

properties_path = 'src/main/resources/'
languages = {
    'de': 'German',
    'es' : 'Spanish',
    'fr' : 'French',
    'it' : 'Italian',
    'ja' : 'Japanese',
    'pt' : 'Portuguese',
    'tr' : 'Turkish',
    'zh' : 'Chinese',
    'ko' : 'Korean',
}

def read_properties_file(filename):
    with open(filename, 'r', encoding='utf-8') as file:
        lines = file.readlines()
    properties = {}
    for line in lines:
        if not line.startswith('#') and '=' in line:
            key, value = line.split('=', 1)
            properties[key.strip()] = value.strip()
    return properties

def create_and_translate_properties_file(target_language_code, target_language_name):
    source_properties = read_properties_file(os.path.join(properties_path, 'messages.properties'))
    new_filename = os.path.join(properties_path, f'messages_{target_language_code}.properties')

    with open(new_filename, 'w', encoding='utf-8') as new_file:
        for key, value in source_properties.items():
            translated_value = translate(value, target_language_name)
            if translated_value:
                new_file.write(f'{key}={translated_value}\n')
            else:
                logging.error(f"Failed to translate key: {key}")
        logging.info(f"Successfully created and populated {new_filename}")

def translate(text, target_language):
    prompt = f"Translate this English text to {target_language}: '{text}'"
    try:
        response = openai.completions.create(
            model="text-davinci-003",
            prompt=prompt,
            temperature=0.3,
            max_tokens=60
        )
        translated_text = response.choices[0].text.strip() if response.choices else None
        return translated_text
    except Exception as e:
        logging.error(f"An error occurred during translation: {e}")
        return None

def update_properties_file(language, key, translated_text):
    filename = os.path.join(properties_path, f'messages_{language}.properties')
    try:
        # Read all lines from the file and normalize line endings
        with open(filename, 'r', encoding='utf-8') as file:
            content = file.read().replace('\r\n', '\n')
            lines = content.split('\n')

        # Update or append the key-value pair
        updated = False
        for i, line in enumerate(lines):
            if line.startswith(f'{key}='):
                lines[i] = f'{key}={translated_text}'
                updated = True
                break

        if not updated:
            lines.append(f'{key}={translated_text}')

        # Ensure consistent line endings and write back to the file
        with open(filename, 'w', encoding='utf-8') as file:
            file.write('\n'.join(lines))

        logging.info(f"Successfully updated {filename} with key: {key}")
    except Exception as e:
        logging.error(f"Failed to update {filename}: {e}")



if __name__ == '__main__':
    logging.basicConfig(level=logging.ERROR)

    default_properties = read_properties_file(os.path.join(properties_path, 'messages.properties'))
    latest_keys = list(default_properties.keys())[-14:]

    for lang_code, lang_name in languages.items():
        translated_properties = read_properties_file(os.path.join(properties_path, f'messages_{lang_code}.properties'))

        for key in latest_keys:
            if key not in translated_properties:
                logging.info(f"Translating key: {key}")
                translated_value = translate(default_properties[key], lang_name)
                if translated_value:
                    logging.info(f"Translated value: {translated_value}")
                    update_properties_file(lang_code, key, translated_value)
                else:
                    logging.error(f"Failed to translate key: {key}")