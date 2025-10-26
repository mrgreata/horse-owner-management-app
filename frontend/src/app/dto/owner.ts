// src/app/dto/owner.ts

/** DTO für Besitzer:innen, die vom Backend kommen */
export interface OwnerDto {
  id: number;
  firstName: string;
  lastName: string;
  email?: string | null;
}

/** DTO für das Erstellen neuer Besitzer:innen */
export interface OwnerCreateDto {
  firstName: string;
  lastName: string;
  email?: string | null; // optional; leeres Feld -> null
}

/** Hilfsfunktion für die Anzeige des vollen Namens */
export const fullName = (o: Pick<OwnerDto, 'firstName' | 'lastName'>) =>
  [o.firstName, o.lastName].filter(Boolean).join(' ');


export type Owner = OwnerDto;
