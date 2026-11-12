using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Controllers.Api
{
    [Route("api/[controller]")]
    [ApiController]
    public class OportunidadeController : ControllerBase
    {
        private readonly MiniCrmContext _context;

        public OportunidadeController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: api/Oportunidade
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Oportunidade>>> GetOportunidade()
        {
            return await _context.Oportunidade.ToListAsync();
        }

        // GET: api/Oportunidade/5
        [HttpGet("{id}")]
        public async Task<ActionResult<Oportunidade>> GetOportunidade(int id)
        {
            var oportunidade = await _context.Oportunidade.FindAsync(id);

            if (oportunidade == null)
            {
                return NotFound();
            }

            return oportunidade;
        }

        // PUT: api/Oportunidade/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutOportunidade(int id, Oportunidade oportunidade)
        {
            if (id != oportunidade.IdOportunidade)
            {
                return BadRequest();
            }

            _context.Entry(oportunidade).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!OportunidadeExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return NoContent();
        }

        // POST: api/Oportunidade
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult<Oportunidade>> PostOportunidade(Oportunidade oportunidade)
        {
            _context.Oportunidade.Add(oportunidade);
            await _context.SaveChangesAsync();

            return CreatedAtAction("GetOportunidade", new { id = oportunidade.IdOportunidade }, oportunidade);
        }

        // DELETE: api/Oportunidade/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteOportunidade(int id)
        {
            var oportunidade = await _context.Oportunidade.FindAsync(id);
            if (oportunidade == null)
            {
                return NotFound();
            }

            _context.Oportunidade.Remove(oportunidade);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool OportunidadeExists(int id)
        {
            return _context.Oportunidade.Any(e => e.IdOportunidade == id);
        }
    }
}
